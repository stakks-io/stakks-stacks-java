/*
 * Copyright © 2022 JINSPIRED B.V.
 */

package io.stakks.stacks.spi.alpha;

import io.humainary.stacks.Stacks;
import io.humainary.substrates.Substrates;
import io.humainary.substrates.Substrates.Name;
import io.humainary.substrates.Substrates.Referent;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.humainary.stacks.Stacks.Stack.Kind.*;
import static java.util.Optional.ofNullable;


/**
 * The implementation of Stacks.Stack interface
 */

final class Stack
  implements Stacks.Stack {

  private static final class Term {
    Stack stack;

    Term (
      final Stack initial
    ) {
      this.stack =
        initial;
    }
  }

  // we use caller and callee as opposed
  // to parent and child respectively
  // simply for better understanding as
  // most processing is call stack related

  private final Stack callee;

  private final Term term;
  private final int  depth;


  // the caller field is "final"
  // as another frame has treated
  // it as the callee (child) of it

  private Stack caller;

  // these fields are changed for
  // each stack capture and collection

  private Name name;
  private Kind kind;


  /*
   * The constructor used by the pool
   */

  Stack () {

    callee =
      null;

    // this struct is shared with all
    // stacks added to this stack

    term =
      new Term (
        this
      );

    depth = 0;

  }

  /*
   * The constructor used for internally
   * pushing stacks on top of each other
   */

  private Stack (
    final Stack callee
  ) {

    this.callee =
      callee;

    callee.caller =
      this;

    this.term =
      callee.term;

    this.depth =
      callee.depth + 1;

  }

  Stack frame (
    final String clasName,
    final String methodName
  ) {

    return
      push (
        Substrates.name (
          clasName,
          methodName
        ),
        FRAME
      );

  }


  Stack exception (
    final Exception exception
  ) {

    return
      push (
        Substrates.name (
          exception.getClass ()
        ),
        EXCEPTION
      );

  }

  Stack site (
    final Referent referent
  ) {

    return
      push (
        referent
          .reference ()
          .name (),
        SITE
      );

  }

  private Stack push (
    final Name name,
    final Kind kind
  ) {

    this.name =
      name;

    this.kind =
      kind;

    return
      caller != null
      ? caller
      : new Stack ( this );

  }


  @Override
  public Optional< Stacks.Stack > enclosure () {

    return
      ofNullable (
        caller ()
      );

  }


  private Stack caller () {

    return
      term.stack != this
      ? caller
      : null;

  }


  // we need to verify the stack is
  // correctly set to ensure that
  // the extent traversal does not
  // go beyond the filled frames

  Stack prepare (
    final Stack top
  ) {

    // top can be null if the stack stream
    // is empty which can occur with some
    // virtual machine runtime configs
    // alternatively we can have a single
    // frame (top == this) which means that
    // there was a value created but filtering
    // excluded all other call stack frames

    if ( top == null || top == this ) {

      return
        null;

    } else {

      // the top is always the next stack to
      // be used as opposed to being filled
      // so, we set the terminal to its callee

      term.stack =
        top.callee;

      return
        this;

    }

  }

  Stack next;


  @Override
  public Kind kind () {

    return
      kind;

  }


  @Override
  public Name name () {

    return
      name;

  }


  @Override
  public Stack extremity () {

    return
      term.stack;

  }

  @Override
  public int depth () {

    return
      term.stack.depth - depth + 1;

  }

  @Override
  public < R > R foldFrom (
    final Function< ? super Stacks.Stack, ? extends R > initial,
    final BiFunction< ? super R, ? super Stacks.Stack, R > accumulator
  ) {

    var stack =
      this;

    var result =
      initial.apply (
        stack
      );


    while ( ( stack = stack.caller () ) != null ) {

      result =
        accumulator.apply (
          result,
          stack
        );

    }

    return
      result;

  }


  @Override
  public < R > R foldTo (
    final Function< ? super Stacks.Stack, ? extends R > initial,
    final BiFunction< ? super R, ? super Stacks.Stack, R > accumulator
  ) {

    var stack =
      extremity ();

    var result =
      initial.apply (
        stack
      );


    while ( ( stack = stack.callee ) != null ) {

      result =
        accumulator.apply (
          result,
          stack
        );

    }

    return
      result;


  }

  @Override
  public Iterator< Stacks.Stack > iterateFrom () {

    return
      new Transverser (
        this
      );

  }


  private static final class Transverser
    implements Iterator< Stacks.Stack > {

    Stack stack;

    Transverser (
      final Stack stack
    ) {

      this.stack =
        stack;

    }

    @Override
    public boolean hasNext () {

      return
        stack != null;

    }

    @Override
    public Stack next () {

      final var result =
        stack;

      if ( result != null ) {

        stack =
          result.caller ();

        return
          result;

      }

      throw
        new NoSuchElementException ();

    }

  }


}
