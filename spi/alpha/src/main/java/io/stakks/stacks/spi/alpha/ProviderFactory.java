/*
 * Copyright © 2022 JINSPIRED B.V.
 */

package io.stakks.stacks.spi.alpha;

import io.humainary.spi.Providers.Factory;
import io.humainary.stacks.spi.StacksProvider;

/**
 * The SPI provider factory implementation of {@code StacksProvider} interface.
 */

public final class ProviderFactory
  implements Factory< StacksProvider > {

  @Override
  public StacksProvider create () {

    return
      new Provider ();

  }

}
