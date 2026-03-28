"use strict";

// ── 레이아웃별 키보드 데이터 ──────────────────────────────────────

const QWERTY_ROWS = [
  ["q", "w", "e", "r", "t", "y", "u", "i", "o", "p"],
  ["a", "s", "d", "f", "g", "h", "j", "k", "l"],
  ["z", "x", "c", "v", "b", "n", "m"],
];

// 두벌식 자모 매핑
const DUBEOLSIK_MAP = {
  q: "ㅂ",
  w: "ㅈ",
  e: "ㄷ",
  r: "ㄱ",
  t: "ㅅ",
  y: "ㅛ",
  u: "ㅕ",
  i: "ㅑ",
  o: "ㅐ",
  p: "ㅔ",
  a: "ㅁ",
  s: "ㄴ",
  d: "ㅇ",
  f: "ㄹ",
  g: "ㅎ",
  h: "ㅗ",
  j: "ㅓ",
  k: "ㅏ",
  l: "ㅣ",
  z: "ㅋ",
  x: "ㅌ",
  c: "ㅊ",
  v: "ㅍ",
  b: "ㅠ",
  n: "ㅜ",
  m: "ㅡ",
  Q: "ㅃ",
  W: "ㅉ",
  E: "ㄸ",
  R: "ㄲ",
  T: "ㅆ",
  O: "ㅒ",
  P: "ㅖ",
};

// 단모음 자모 매핑 (1회/2회)
const DANMOEM_MAP = {
  q: ["ㅂ", "ㅃ"],
  w: ["ㅈ", "ㅉ"],
  e: ["ㄷ", "ㄸ"],
  r: ["ㄱ", "ㄲ"],
  t: ["ㅅ", "ㅆ"],
  y: ["ㅗ", "ㅛ"],
  u: ["ㅐ", "ㅒ"],
  i: ["ㅔ", "ㅖ"],
  a: ["ㅁ"],
  s: ["ㄴ"],
  d: ["ㅇ"],
  f: ["ㄹ"],
  g: ["ㅎ"],
  h: ["ㅓ", "ㅕ"],
  j: ["ㅏ", "ㅑ"],
  k: ["ㅣ"],
  z: ["ㅋ"],
  x: ["ㅌ"],
  c: ["ㅊ"],
  v: ["ㅍ"],
  b: ["ㅜ", "ㅠ"],
  n: ["ㅡ"],
  m: ["ㅡ"],
};

// 천지인 (3×4 폰 키패드)
const CHEONJIIN_KEYS = [
  { key: "1", label: "1", jamo: "ㅣ" },
  { key: "2", label: "2", jamo: "ㆍ" },
  { key: "3", label: "3", jamo: "ㅡ" },
  { key: "4", label: "4", jamo: "ㄱ ㅋ ㄲ" },
  { key: "5", label: "5", jamo: "ㄴ ㄹ" },
  { key: "6", label: "6", jamo: "ㄷ ㅌ ㄸ" },
  { key: "7", label: "7", jamo: "ㅂ ㅍ ㅃ" },
  { key: "8", label: "8", jamo: "ㅅ ㅎ ㅆ" },
  { key: "9", label: "9", jamo: "ㅈ ㅊ ㅉ" },
  { key: "BACKSPACE", label: "⌫", jamo: "", special: true },
  { key: "0", label: "0", jamo: "ㅇ ㅁ" },
  { key: "SPACE", label: "␣", jamo: "", special: true },
];

// KT 나랏글 (10-key + * #)
const NARATGEUL_KEYS = [
  { key: "1", label: "1", jamo: "ㄱ ㅋ" },
  { key: "2", label: "2", jamo: "ㄴ ㄷ ㅌ" },
  { key: "3", label: "3", jamo: "ㅏ ㅓ" },
  { key: "4", label: "4", jamo: "ㄹ" },
  { key: "5", label: "5", jamo: "ㅁ ㅂ ㅍ" },
  { key: "6", label: "6", jamo: "ㅗ ㅜ" },
  { key: "7", label: "7", jamo: "ㅅ ㅈ ㅊ" },
  { key: "8", label: "8", jamo: "ㅇ ㅎ" },
  { key: "9", label: "9", jamo: "ㅣ" },
  { key: "STROKE_ADD", label: "*", jamo: "획추가", special: true },
  { key: "0", label: "0", jamo: "ㅡ" },
  { key: "#", label: "#", jamo: "쌍자음", special: true },
  { key: "BACKSPACE", label: "⌫", jamo: "", special: true },
  { key: "SPACE", label: "␣", jamo: "", special: true },
  { key: "ENTER", label: "↵", jamo: "", special: true },
];

// SKY-II (12-key 멀티탭)
const SKYII_KEYS = [
  { key: "1", label: "1", jamo: "ㄱ ㅋ ㄲ" },
  { key: "2", label: "2", jamo: "ㅣ ㅡ" },
  { key: "3", label: "3", jamo: "ㅏ ㅑ" },
  { key: "4", label: "4", jamo: "ㄷ ㅌ ㄸ" },
  { key: "5", label: "5", jamo: "ㄴ ㄹ" },
  { key: "6", label: "6", jamo: "ㅓ ㅕ" },
  { key: "7", label: "7", jamo: "ㅁ ㅅ" },
  { key: "8", label: "8", jamo: "ㅂ ㅍ ㅃ" },
  { key: "9", label: "9", jamo: "ㅗ ㅛ" },
  { key: "*", label: "*", jamo: "ㅈ ㅊ ㅉ" },
  { key: "0", label: "0", jamo: "ㅇ ㅎ" },
  { key: "#", label: "#", jamo: "ㅜ ㅠ" },
  { key: "BACKSPACE", label: "⌫", jamo: "", special: true },
  { key: "SPACE", label: "␣", jamo: "", special: true },
  { key: "ENTER", label: "↵", jamo: "", special: true },
];

// 모토로라 (12-key + # 변환키)
const MOTOROLA_KEYS = [
  { key: "1", label: "1", jamo: "ㄱ ㅋ" },
  { key: "2", label: "2", jamo: "ㄴ ㅁ" },
  { key: "3", label: "3", jamo: "ㅏ ㅓ" },
  { key: "4", label: "4", jamo: "ㄷ ㅌ" },
  { key: "5", label: "5", jamo: "ㄹ" },
  { key: "6", label: "6", jamo: "ㅗ ㅜ" },
  { key: "7", label: "7", jamo: "ㅂ ㅍ" },
  { key: "8", label: "8", jamo: "ㅅ" },
  { key: "9", label: "9", jamo: "ㅣ ㅡ" },
  { key: "*", label: "*", jamo: "ㅈ ㅊ" },
  { key: "0", label: "0", jamo: "ㅇ ㅎ" },
  { key: "MODE_SWITCH", label: "#", jamo: "변환", special: true },
  { key: "BACKSPACE", label: "⌫", jamo: "", special: true },
  { key: "SPACE", label: "␣", jamo: "", special: true },
  { key: "ENTER", label: "↵", jamo: "", special: true },
];

const PHONE_LAYOUT_KEYS = {
  cheonjiin: CHEONJIIN_KEYS,
  naratgeul: NARATGEUL_KEYS,
  skyii: SKYII_KEYS,
  motorola: MOTOROLA_KEYS,
};

const PHONE_LAYOUTS = new Set(["cheonjiin", "naratgeul", "skyii", "motorola"]);

// ── 상태 ──────────────────────────────────────────────────────────

let sessionId = null;
let currentLayout = "dubeolsik";
let shiftActive = false;

// ── DOM 참조 ──────────────────────────────────────────────────────

const layoutSelect = document.getElementById("layout-select");
const resetBtn = document.getElementById("reset-btn");
const committedEl = document.getElementById("committed");
const composingEl = document.getElementById("composing");
const keyboardEl = document.getElementById("keyboard");
const predictionsList = document.getElementById("predictions-list");

const debugFsm = document.getElementById("d-fsm");
const debugCho = document.getElementById("d-cho");
const debugJung = document.getElementById("d-jung");
const debugJong = document.getElementById("d-jong");
const debugJong2 = document.getElementById("d-jong2");
const debugCycle = document.getElementById("d-cycle");

let lastPredictions = [];
let lastKeyHints = {};

// ── API 호출 ───────────────────────────────────────────────────────

async function createSession(layout) {
  const form = new URLSearchParams({ layout });
  const res = await fetch("/api/session", { method: "POST", body: form });
  const data = await res.json();
  return data.sessionId;
}

async function sendInput(type, key) {
  if (!sessionId) return;
  const res = await fetch(`/api/session/${sessionId}/input`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ type, key }),
  });
  if (!res.ok) return;
  const data = await res.json();
  updateDisplay(data);
}

async function sendFlush() {
  if (!sessionId) return;
  const res = await fetch(`/api/session/${sessionId}/flush`, {
    method: "POST",
  });
  if (!res.ok) return;
  const data = await res.json();
  updateDisplay(data);
}

async function deleteSession() {
  if (!sessionId) return;
  await fetch(`/api/session/${sessionId}`, { method: "DELETE" });
  sessionId = null;
}

// ── 화면 업데이트 ─────────────────────────────────────────────────

function updateDisplay(data) {
  committedEl.textContent = data.committed;
  composingEl.textContent = data.composing;

  debugFsm.textContent = data.fsm;
  debugCho.textContent = data.cho ?? "-";
  debugJung.textContent = data.jung ?? "-";
  debugJong.textContent = data.jong ?? "-";
  debugJong2.textContent = data.jong2 ?? "-";
  debugCycle.textContent = data.cycleCount;

  // 예측 후보 표시
  if (data.predictions) {
    displayPredictions(data.predictions);
    lastPredictions = data.predictions;
  }

  // 키 힌트 적용
  if (data.nextKeyHints) {
    applyKeyHints(data.nextKeyHints);
    lastKeyHints = data.nextKeyHints;
  }
}

function clearDisplay() {
  committedEl.textContent = "";
  composingEl.textContent = "";
  debugFsm.textContent = "S0";
  debugCho.textContent =
    debugJung.textContent =
    debugJong.textContent =
    debugJong2.textContent =
      "-";
  debugCycle.textContent = "0";
  predictionsList.innerHTML = "";
  lastPredictions = [];
  lastKeyHints = {};
  clearKeyHints();
}

// ── 예측 표시 ─────────────────────────────────────────────────────

function displayPredictions(predictions) {
  predictionsList.innerHTML = "";

  if (!predictions || predictions.length === 0) {
    return;
  }

  predictions.forEach((pred) => {
    const item = document.createElement("div");
    item.className = `prediction-item${pred.isUserWord ? " user-word" : ""}`;
    item.textContent = pred.word;
    item.title = `Score: ${pred.score.toFixed(2)}${pred.isUserWord ? " (사용자 단어)" : ""}`;
    item.addEventListener("click", () => {
      // TODO: 예측 단어 선택 처리
      console.log("Selected prediction:", pred.word);
    });
    predictionsList.appendChild(item);
  });
}

// ── 키 힌트 적용 ─────────────────────────────────────────────────

function applyKeyHints(keyHints) {
  clearKeyHints();

  if (!keyHints || Object.keys(keyHints).length === 0) {
    return;
  }

  // 모든 키 버튼에서 hint 클래스 제거
  keyboardEl.querySelectorAll(".key").forEach((keyBtn) => {
    keyBtn.classList.remove("hint");
    keyBtn.style.removeProperty("--hint-weight");
  });

  // 새로운 힌트 적용
  Object.entries(keyHints).forEach(([key, weight]) => {
    const keyBtn = keyboardEl.querySelector(`[data-key="${CSS.escape(key)}"]`);
    if (keyBtn) {
      keyBtn.classList.add("hint");
      keyBtn.style.setProperty("--hint-weight", weight.toFixed(2));
    }
  });
}

function clearKeyHints() {
  keyboardEl.querySelectorAll(".key.hint").forEach((keyBtn) => {
    keyBtn.classList.remove("hint");
    keyBtn.style.removeProperty("--hint-weight");
  });
}

// ── 키보드 빌더 ───────────────────────────────────────────────────

function buildQwertyKeyboard(layout) {
  keyboardEl.className = "qwerty";
  keyboardEl.innerHTML = "";

  const map = layout === "danmoem" ? null : DUBEOLSIK_MAP;

  QWERTY_ROWS.forEach((row, ri) => {
    const rowEl = document.createElement("div");
    rowEl.className = "kbd-row";

    if (ri === 2) {
      rowEl.appendChild(
        makeSpecialKey("SHIFT", "⇧", shiftActive ? "wide active" : "wide"),
      );
    }

    row.forEach((ch) => {
      const upper = ch.toUpperCase();
      let normal = "",
        shifted = "";

      if (layout === "danmoem") {
        const arr = DANMOEM_MAP[ch] || [];
        normal = arr[0] || "";
        shifted = arr[1] || "";
      } else {
        normal = DUBEOLSIK_MAP[ch] || "";
        shifted = DUBEOLSIK_MAP[upper] || "";
      }

      const btn = document.createElement("button");
      btn.className = "key";
      btn.dataset.inputType = "char";
      btn.dataset.key = shiftActive ? upper : ch;

      btn.innerHTML =
        `<span class="key-latin">${shiftActive ? upper : ch}</span>` +
        `<span class="key-korean">${shiftActive && shifted ? shifted : normal}</span>`;

      if (shiftActive && shifted) btn.classList.add("key-shift");

      btn.addEventListener("click", () => handleKeyClick(btn));
      rowEl.appendChild(btn);
    });

    if (ri === 2) {
      rowEl.appendChild(makeSpecialKey("BACKSPACE", "⌫", "wide special"));
    }

    keyboardEl.appendChild(rowEl);
  });

  // 하단 행: 공백, 엔터
  const bottomRow = document.createElement("div");
  bottomRow.className = "kbd-row";
  bottomRow.appendChild(makeSpecialKey("SPACE", "공백", "space special"));
  bottomRow.appendChild(makeSpecialKey("ENTER", "↵", "wide special"));
  keyboardEl.appendChild(bottomRow);
}

function buildPhoneKeyboard(keys) {
  keyboardEl.className = "phone";
  keyboardEl.innerHTML = "";

  for (let i = 0; i < keys.length; i += 3) {
    const rowEl = document.createElement("div");
    rowEl.className = "kbd-row";

    keys.slice(i, i + 3).forEach((k) => {
      const btn = document.createElement("button");

      if (k.special) {
        btn.className = "key special";
        btn.dataset.inputType = "special";
        btn.dataset.key = k.key;
        btn.innerHTML =
          `<span class="key-label">${k.label}</span>` +
          `<span class="key-korean">${k.jamo}</span>`;
      } else {
        btn.className = "key";
        btn.dataset.inputType = "char";
        btn.dataset.key = k.key;
        btn.innerHTML =
          `<span class="key-label">${k.label}</span>` +
          `<span class="key-korean">${k.jamo}</span>`;
      }

      btn.addEventListener("click", () => handleKeyClick(btn));
      rowEl.appendChild(btn);
    });

    // 나랏글 # 키는 char로 전달
    if (currentLayout === "naratgeul") {
      rowEl.querySelectorAll('[data-key="#"]').forEach((btn) => {
        btn.dataset.inputType = "char";
      });
    }

    keyboardEl.appendChild(rowEl);
  }
}

function makeSpecialKey(specialKey, label, extraClass = "") {
  const btn = document.createElement("button");
  btn.className = `key special ${extraClass}`.trim();
  btn.dataset.inputType = "special";
  btn.dataset.key = specialKey;
  btn.innerHTML = `<span class="key-korean">${label}</span>`;
  btn.addEventListener("click", () => handleKeyClick(btn));
  return btn;
}

function rebuildKeyboard() {
  if (PHONE_LAYOUTS.has(currentLayout)) {
    buildPhoneKeyboard(PHONE_LAYOUT_KEYS[currentLayout]);
  } else {
    buildQwertyKeyboard(currentLayout);
  }
}

// ── 입력 처리 ─────────────────────────────────────────────────────

function handleKeyClick(btn) {
  const type = btn.dataset.inputType;
  const key = btn.dataset.key;

  if (type === "special" && key === "SHIFT") {
    shiftActive = !shiftActive;
    rebuildKeyboard();
    return;
  }

  btn.classList.add("pressed");
  setTimeout(() => btn.classList.remove("pressed"), 100);

  sendInput(type, key);
}

// 물리 키보드 (두벌식/단모음 전용)
document.addEventListener("keydown", (e) => {
  if (PHONE_LAYOUTS.has(currentLayout)) return;
  if (e.target === layoutSelect) return;
  e.preventDefault();

  if (e.key === "Backspace") {
    sendInput("special", "BACKSPACE");
    return;
  }
  if (e.key === "Enter") {
    sendInput("special", "ENTER");
    return;
  }
  if (e.key === " ") {
    sendInput("special", "SPACE");
    return;
  }
  if (e.key === "Shift") return;

  if (e.key.length === 1) {
    sendInput("char", e.key);
  }
});

// ── 초기화 ────────────────────────────────────────────────────────

async function init() {
  // 레이아웃 목록 가져오기
  const res = await fetch("/api/layouts");
  const layouts = await res.json();

  layouts.forEach((l) => {
    const opt = document.createElement("option");
    opt.value = l.key;
    opt.textContent = l.displayName;
    layoutSelect.appendChild(opt);
  });

  currentLayout = layouts[0]?.key ?? "dubeolsik";
  layoutSelect.value = currentLayout;

  sessionId = await createSession(currentLayout);
  rebuildKeyboard();
}

layoutSelect.addEventListener("change", async () => {
  await sendFlush();
  await deleteSession();
  currentLayout = layoutSelect.value;
  shiftActive = false;
  sessionId = await createSession(currentLayout);
  clearDisplay();
  rebuildKeyboard();
});

resetBtn.addEventListener("click", async () => {
  await deleteSession();
  sessionId = await createSession(currentLayout);
  clearDisplay();
});

init();
