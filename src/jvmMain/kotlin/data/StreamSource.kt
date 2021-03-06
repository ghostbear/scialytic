package data

enum class StreamSource(
    val displayName: String,
    val url: String,
    val gateway: String,
) {
    J_POP(
        "J-Pop",
        "https://listen.moe/stream",
        "wss://listen.moe/gateway_v2"
    ),
    K_POP(
        "K-Pop",
        "https://listen.moe/kpop/stream",
         "wss://listen.moe/kpop/gateway_v2"
    );
}