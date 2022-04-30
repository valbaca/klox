package lox

/**
 * Exit codes, c/o BSD
 * https://www.freebsd.org/cgi/man.cgi?query=sysexits&apropos=0&sektion=0&manpath=FreeBSD+4.3-RELEASE&format=html
 */
enum class ExitCodes(val value: Int) {
    USAGE(64),
    DATAERR(65)
}