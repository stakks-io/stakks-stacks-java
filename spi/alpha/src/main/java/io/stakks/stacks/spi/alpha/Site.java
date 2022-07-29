/*
 * Copyright © 2022 JINSPIRED B.V.
 */

package io.stakks.stacks.spi.alpha;

import io.humainary.stacks.Stacks;
import io.humainary.substrates.Substrates.Inlet;
import io.humainary.substrates.sdk.AbstractInstrument;

import java.util.function.UnaryOperator;

import static io.stakks.stacks.spi.alpha.StackTraces.calls;

/**
 * The implementation of {@code Stacks.Site} interface
 */

final class Site
  extends AbstractInstrument< Stacks.Stack >
  implements Stacks.Site {

  Site (
    final Inlet< Stacks.Stack > inlet
  ) {

    super (
      inlet
    );

  }

  @Override
  public void emit (
  ) {

    emit (
      1
    );

  }


  @Override
  public void emit (
    final int skip
  ) {

    emit (
      stack ->
        stack.prepare (
          Collectors
            .of ( reference () )
            .collect (
              this,
              stack,
              skip + 3
            )
        )
    );

  }

  @Override
  public void emit (
    final Exception exception
  ) {

    emit (
      stack ->
        stack.prepare (
          calls (
            stack.exception (
              exception
            ),
            exception.getStackTrace (),
            0
          )
        )
    );

  }

  private void emit (
    final UnaryOperator< Stack > func
  ) {

    // to allow for allocation cost
    // reductions a pool interface
    // is employed to allow reuse

    final var pool =
      Pools.of (
        this
      );

    // a stack is only valid for the
    // lifetime of the event emittance
    // consumers should extract all
    // contents within the scope of
    // the event (emittance) dispatch

    final var stack =
      pool.reserve ();

    try {

      final var result =
        func.apply (
          stack
        );

      // only publish if a
      // stack was collected

      if ( result != null ) {

        inlet.emit (
          result
        );

      }

    } finally {

      pool.release (
        stack
      );

    }

  }


}
