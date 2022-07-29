/*
 * Copyright © 2022 JINSPIRED B.V.
 */

package io.stakks.stacks.spi.alpha;

import io.humainary.stacks.Stacks;
import io.humainary.stacks.spi.StacksProvider;
import io.humainary.substrates.sdk.AbstractContextProvider;

/**
 * The SPI implementation of {@code StacksProvider} interface.
 */

final class Provider
  extends AbstractContextProvider< Stacks.Site, Context >
  implements StacksProvider {

  Provider () {

    super (
      environment ->
        new Context (
          environment,
          Site::new
        )
    );

  }

}
