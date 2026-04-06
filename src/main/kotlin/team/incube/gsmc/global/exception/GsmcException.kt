package team.incube.gsmc.global.exception

open class GsmcException(
    val errorCode: ErrorCode,
) : RuntimeException(errorCode.message)
