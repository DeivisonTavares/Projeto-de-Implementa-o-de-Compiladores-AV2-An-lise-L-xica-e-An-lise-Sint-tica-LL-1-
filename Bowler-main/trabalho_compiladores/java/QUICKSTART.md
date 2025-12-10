# Quick Start — Bowler LL(1) Parser

Comece em 5 minutos!

## Setup

```bash
cd trabalho_compiladores/java
javac -d bin src/*.java
```

## Execute um Teste

```bash
java -cp bin Bowler src/teste_string.bw
```

Você verá:
```
✅ Parser Recursivo: Programa reconhecido sem erros.
✅ Análise SUCESSO! String aceita.
```

## Seu Primeiro Programa

Crie `meu_prog.bw`:
```bowler
main {
  var x: int = 10;
  var nome: string = "Alice";
  
  if (x > 5) {
    print(x);
  } else {
    print(0);
  }
}
```

Execute:
```bash
java -cp bin Bowler meu_prog.bw
```

## Features

**Tipos**: `int`, `float`, `string`

**Operadores**:
- Aritméticos: `+`, `-`, `*`, `/`
- Comparadores: `>`, `<`, `==`, `!=`, `<=`, `>=`
- Lógicos: `&&`, `||`

**Estruturas de Controle**:
```bowler
if (c) { ... } else { ... }
while (c) { ... }
do { ... } while (c);
for (init; cond; step) { ... }
print(expr);
return expr;
```

## Exemplos

**String**:
```bowler
main {
  var msg: string = "Hello";
  print(msg);
}
```

**Comparadores**:
```bowler
main {
  var x: int = 10;
  if (x > 5) { print(1); }
}
```

**Lógicos**:
```bowler
main {
  var x: int = 10;
  if (x > 5 && x != 0) { print(x); }
}
```

**Do/While**:
```bowler
main {
  var x: int = 3;
  do {
    print(x);
    x = x - 1;
  } while (x > 0);
}
```

**For**:
```bowler
main {
  for (var i: int = 0; (i < 5); i = i + 1) {
    print(i);
  }
}
```

## Todos os Testes

```bash
for f in src/teste_*.bw; do
  java -cp bin Bowler "$f" 2>&1 | grep "✅"
done
```

Esperado: **17/17 testes passando** ✅

## Docs

- **README.md** — Overview
- **LL1_ANALYSIS.md** — Análise LL(1)
- **IMPLEMENTATION.md** — Implementação
- **TESTES.md** — Testes

