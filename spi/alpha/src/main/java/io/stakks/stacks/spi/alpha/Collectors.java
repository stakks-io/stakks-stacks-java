/*
 * Copyright © 2022 JINSPIRED B.V.
 */

package io.stakks.stacks.spi.alpha;

import io.humainary.stacks.Stacks;
import io.humainary.substrates.Substrates;
import io.humainary.substrates.Substrates.Environment;
import io.humainary.substrates.Substrates.Substrate;
import io.humainary.substrates.Substrates.Variable;

import java.lang.StackWalker.Option;

import static io.humainary.stacks.Stacks.Context.LIMIT;
import static io.humainary.substrates.Substrates.name;
import static io.humainary.substrates.Substrates.variable;
import static io.stakks.stacks.spi.alpha.StackTraces.calls;
import static java.lang.Math.max;
import static java.lang.StackWalker.getInstance;
import static java.lang.Thread.currentThread;
import static java.util.EnumSet.noneOf;

final class Collectors {

  private static final String WALKER    = "walker";
  private static final String THROWABLE = "throwable";
  private static final String SIMPLE    = "simple";
  private static final String THREAD    = "thread";
  private static final String CALLER    = "caller";


  private static final Substrates.Name COLLECTOR_NAME =
    name ( Collector.class );

  private static final Variable< Collector > COLLECTOR =
    variable (
      COLLECTOR_NAME,
      Collector.class,
      ( site, stack, skip ) -> stack
    );


  @FunctionalInterface
  interface Collector {

    Stack collect (
      Site site,
      Stack stack,
      int skip
    );

  }

  static Environment install (
    final Environment environment
  ) {


    return
      environment
        .override (
          COLLECTOR_NAME,
          collector (
            environment,
            environment.getString (
              Stacks.Context.COLLECTOR,
              WALKER
            )
          )
        );

  }


  private static Collector collector (
    final Environment environment,
    final String option
  ) {

    return
      option.equals ( WALKER )
      ? new Walker ( environment )
      : option.equals ( THROWABLE )
        ? new Throwable ()
        : option.equals ( SIMPLE )
          ? new Simple ()
          : option.equals ( THREAD )
            ? new Thread ()
            : option.equals ( CALLER )
              ? new Caller ()
              : new Walker ( environment );

  }


  static Collector of (
    final Substrate substrate
  ) {

    return
      COLLECTOR.of (
        substrate
      );

  }


  private static final class Walker
    implements Collector {

    private final StackWalker stackWalker;
    private final int         limit;

    Walker (
      final Environment environment
    ) {

      stackWalker =
        getInstance (
          noneOf ( Option.class ),
          max (
            limit =
              environment.getInteger (
                LIMIT,
                128
              ),
            0
          )
        );

    }

    @Override
    public Stack collect (
      final Site site,
      final Stack stack,
      final int skip
    ) {

      return
        stackWalker.walk (
          frames ->
            frames
              .skip ( ( skip + 1 ) )
              .limit ( limit )
              .reduce (
                stack,
                ( current, frame ) ->
                  current.frame (
                    frame.getClassName (),
                    frame.getMethodName ()
                  ),
                ( left, right ) ->
                  left
              )
        );

    }

  }


  private static final class Caller
    implements Collector {

    private final StackWalker walker;

    Caller () {

      walker =
        getInstance (
          noneOf (
            Option.class
          )
        );

    }

    @Override
    public Stack collect (
      final Site site,
      final Stack stack,
      final int skip
    ) {

      return
        walker.walk (
          frames ->
            frames
              .skip ( ( skip + 1 ) )
              .limit ( 1 )
              .findFirst ()
              .map (
                frame ->
                  stack.frame (
                    frame.getClassName (),
                    frame.getMethodName ()
                  ) )
              .orElse ( stack )
        );

    }

  }


  private static final class Throwable
    implements Collector {

    @Override
    public Stack collect (
      final Site site,
      final Stack stack,
      final int skip
    ) {

      return
        calls (
          stack,
          new java.lang.Throwable ()
            .getStackTrace (),
          skip + 2
        );

    }

  }


  private static final class Thread
    implements Collector {

    @Override
    public Stack collect (
      final Site site,
      final Stack stack,
      final int skip
    ) {

      return
        calls (
          stack,
          currentThread ()
            .getStackTrace (),
          skip + 3
        );

    }

  }


  private static final class Simple
    implements Collector {

    @Override
    public Stack collect (
      final Site site,
      final Stack stack,
      final int skip
    ) {

      return
        stack.site (
          site
        );

    }

  }


}
