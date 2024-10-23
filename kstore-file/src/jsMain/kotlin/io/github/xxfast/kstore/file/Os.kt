package io.github.xxfast.kstore.file

internal external interface Os {
  /**
   * See https://nodejs.org/api/os.html#oshomedir
   */
  fun homedir(): String
  /**
   * See https://nodejs.org/api/os.html#ostmpdir
   */
  fun tmpdir(): String
}