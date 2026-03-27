package `in`.daram.nutcracker.prediction.trie

internal class TrieNode {
    val children: HashMap<Char, TrieNode> = HashMap()
    var terminalScore: Float? = null
}
