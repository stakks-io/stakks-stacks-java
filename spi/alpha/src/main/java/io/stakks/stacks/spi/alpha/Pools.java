/*
 * Copyright © 2022 JINSPIRED B.V.
 */

package io.stakks.stacks.spi.alpha;

import io.humainary.substrates.Substrates.Environment;
import io.humainary.substrates.Substrates.Name;
import io.humainary.substrates.Substrates.Substrate;
import io.humainary.substrates.Substrates.Variable;

import java.util.concurrent.atomic.AtomicReference;

import static io.humainary.substrates.Substrates.name;
import static io.humainary.substrates.Substrates.variable;

final class Pools {

  interface Pool {

    // this is the name used to
    // look up the pool impl within
    // the substrate's environment

    Stack reserve ();

    default void release (
      final Stack stack
    ) {
      // there is no reuse across
      // site event emittances
    }

  }

  private static final Name NAME =
    name (
      Pool.class
    );

  private static final Variable< Pool > POOL =
    variable (
      NAME,
      Pool.class,
      Stack::new
    );


  static Pool of (
    final Substrate substrate
  ) {

    return
      POOL.of (
        substrate
      );

  }


  static Environment install (
    final Environment environment
  ) {

    // in the future we can use the
    // environment to decide on the
    // choice of pool impl to be used

    return
      environment
        .override (
          NAME,
          new Cache ()
        );

  }


  private static final class Cache
    implements Pool {


    // if we start caching at the site level
    // then we might need to change to use
    // a varhandle or an atomic field updater

    private final AtomicReference< Stack > stacks =
      new AtomicReference<> (
        new Stack ()
      );


    // remove a stack from the pool or if
    // none available we create a new one

    @Override
    public Stack reserve () {

      Stack stack;

      while (
        ( stack = stacks.get () ) != null
      ) {

        // remove the stack from the pool and then
        // set the value to the stack.next value

        if (
          stacks.compareAndSet (
            stack,
            stack.next
          )
        ) {

          return
            stack;

        }

      }

      // create a new stack

      return
        new Stack ();

    }


    // add the stack back into the pool

    @Override
    public void release (
      final Stack stack
    ) {

      // in the future we can do a backoff
      // and simply discard the stack ref
      // after a number of cas attempts

      do {

        // optimistically assume
        // the stack will be set

        stack.next =
          stacks.get ();

      } while (
        !stacks.compareAndSet (
          stack.next,
          stack
        )
      );

    }

  }

}
