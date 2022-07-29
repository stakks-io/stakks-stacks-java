/*
 * Copyright © 2022 JINSPIRED B.V.
 */

package io.stakks.stacks.spi.alpha;

final class StackTraces {

  private StackTraces () {}

  static Stack calls (
    final Stack stack,
    final StackTraceElement[] frames,
    final int skip
  ) {

    var skipped = skip;

    var result = stack;

    for ( final StackTraceElement frame : frames ) {

      if ( --skipped > 0 )
        continue;

      final var className =
        frame.getClassName ();

      if (
        !className.startsWith ( "jdk.internal." ) &&
          !className.startsWith ( "java.lang.reflect" )
      ) {

        result =
          result.frame (
            className,
            frame.getMethodName ()
          );

      }

    }

    return
      result;

  }

}
