package org.ahocorasick_fork2.trie.handler;

import java.util.List;

import org.ahocorasick_fork2.trie.Emit;

public interface StatefulEmitHandler extends EmitHandler {
    List<Emit> getEmits();
}
