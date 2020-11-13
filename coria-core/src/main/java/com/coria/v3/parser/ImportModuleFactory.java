package com.coria.v3.parser;

import java.util.List;

/**
 * Abstraction interface to support factory classes that instantiate instances of ImportModuleBase.
 * Created by David Fradin, 2020
 *
 * @param <T>
 */
public interface ImportModuleFactory<T extends ImportModuleBase> {
    List<? extends T> getList();
}
