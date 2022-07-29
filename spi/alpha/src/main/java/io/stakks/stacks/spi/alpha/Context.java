/*
 * Copyright © 2022 JINSPIRED B.V.
 */

package io.stakks.stacks.spi.alpha;

import io.humainary.stacks.Stacks;
import io.humainary.substrates.Substrates.Environment;
import io.humainary.substrates.Substrates.Name;
import io.humainary.substrates.Substrates.Type;
import io.humainary.substrates.sdk.AbstractContext;

import static io.humainary.stacks.Stacks.Site.TYPE;

/**
 * The implementation of the {@code Stacks.Context} interface
 */

final class Context
  extends AbstractContext< Stacks.Site, Stacks.Stack >
  implements Stacks.Context {

  Context (
    final Environment environment,
    final Producer< ? extends Stacks.Site, Stacks.Stack > producer
  ) {

    super (
      Collectors.install (
        Pools.install (
          environment
        )
      ),
      producer
    );

  }


  @Override
  protected Type type () {

    return
      TYPE;

  }


  @Override
  public Stacks.Site site (
    final Name name
  ) {

    return
      instrument (
        name
      );

  }

}
