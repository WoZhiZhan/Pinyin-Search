package org.ahocorasick_fork2.trie.handler;

import org.ahocorasick_fork2.trie.PayloadEmit;

public interface PayloadEmitHandler<T> {
    boolean emit(PayloadEmit<T> emit);
}
