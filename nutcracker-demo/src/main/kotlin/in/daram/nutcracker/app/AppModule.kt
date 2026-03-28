package `in`.daram.nutcracker.app

import `in`.daram.nutcracker.KeyInput
import `in`.daram.nutcracker.SpecialKey
import `in`.daram.nutcracker.prediction.InputLanguage
import `in`.daram.nutcracker.prediction.KeyMapper
import `in`.daram.nutcracker.prediction.PredictionQuery
import `in`.daram.nutcracker.prediction.WordPredictor
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*

fun Application.configureApp() {
    install(ContentNegotiation) {
        json()
    }
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.ContentType)
    }

    routing {
        staticResources("/", "static") {
            default("index.html")
        }

        route("/api") {
            get("/layouts") {
                call.respond(AutomataRegistry.layouts.map { LayoutResponse(it.key, it.displayName) })
            }

            post("/session") {
                val params = call.receiveParameters()
                val layout = params["layout"] ?: "dubeolsik"
                if (layout !in AutomataRegistry.all) {
                    call.respond(HttpStatusCode.BadRequest, "Unknown layout: $layout")
                    return@post
                }
                val session = SessionStore.create(layout)
                call.respond(SessionResponse(session.id, layout))
            }

            route("/session/{id}") {
                delete {
                    val id = call.parameters["id"]!!
                    SessionStore.delete(id)
                    call.respond(HttpStatusCode.NoContent)
                }

                post("/input") {
                    val id = call.parameters["id"]!!
                    val session = SessionStore.get(id) ?: run {
                        call.respond(HttpStatusCode.NotFound, "Session not found")
                        return@post
                    }
                    val req = call.receive<InputRequest>()
                    val automata = AutomataRegistry.all[session.layout]!!

                    val keyInput = when (req.type) {
                        "char" -> {
                            val ch = req.key.singleOrNull() ?: run {
                                call.respond(HttpStatusCode.BadRequest, "key must be a single character")
                                return@post
                            }
                            KeyInput.Char(ch)
                        }

                        "special" -> {
                            val specialKey = runCatching { SpecialKey.valueOf(req.key) }.getOrNull() ?: run {
                                call.respond(HttpStatusCode.BadRequest, "Unknown special key: ${req.key}")
                                return@post
                            }
                            KeyInput.Special(specialKey)
                        }

                        else -> {
                            call.respond(HttpStatusCode.BadRequest, "type must be 'char' or 'special'")
                            return@post
                        }
                    }

                    val result = automata.process(session.state, keyInput)

                    // "\b" 처리: 마지막 확정 문자 제거
                    for (ch in result.committed) {
                        if (ch == '\b') {
                            if (session.committed.isNotEmpty()) {
                                session.committed = session.committed.dropLast(1)
                            }
                        } else {
                            session.committed += ch
                        }
                    }
                    session.state = result.newState

                    call.respond(buildResponse(session, result.composing))
                }

                post("/flush") {
                    val id = call.parameters["id"]!!
                    val session = SessionStore.get(id) ?: run {
                        call.respond(HttpStatusCode.NotFound, "Session not found")
                        return@post
                    }
                    val automata = AutomataRegistry.all[session.layout]!!
                    val result = automata.flush(session.state)

                    for (ch in result.committed) {
                        if (ch == '\b') {
                            if (session.committed.isNotEmpty()) {
                                session.committed = session.committed.dropLast(1)
                            }
                        } else {
                            session.committed += ch
                        }
                    }
                    session.state = result.newState

                    call.respond(buildResponse(session, result.composing))
                }
            }
        }
    }
}

private fun buildResponse(
    session: Session,
    composing: String
): InputResponse {
    val s = session.state

    // 예측 후보 및 키 힌트 생성
    val (predictions, nextKeyHints) = try {
        val predictor = PredictorRegistry.getPredictor(session.layout)
        val keyMapper = PredictorRegistry.getKeyMapper(session.layout)

        if (predictor != null && keyMapper != null) {
            val query = PredictionQuery(
                committedText = session.committed,
                composingText = composing,
                composingState = s,
                language = InputLanguage.KOREAN,
                maxResults = 5,
            )
            val candidates = predictor.predict(query)
            val hints = predictor.nextKeyHints(candidates, keyMapper)

            val predictionsDto = candidates.map {
                PredictionCandidateDto(
                    word = it.word,
                    score = it.score,
                    isUserWord = it.isUserWord,
                )
            }
            predictionsDto to hints.keyHints
        } else {
            emptyList<PredictionCandidateDto>() to emptyMap()
        }
    } catch (e: Exception) {
        emptyList<PredictionCandidateDto>() to emptyMap()
    }

    return InputResponse(
        committed = session.committed,
        composing = composing,
        fsm = s.fsm.name,
        cho = s.cho?.toString(),
        jung = s.jung?.toString(),
        jong = s.jong?.toString(),
        jong2 = s.jong2?.toString(),
        cycleCount = s.cycleCount,
        predictions = predictions,
        nextKeyHints = nextKeyHints,
    )
}
