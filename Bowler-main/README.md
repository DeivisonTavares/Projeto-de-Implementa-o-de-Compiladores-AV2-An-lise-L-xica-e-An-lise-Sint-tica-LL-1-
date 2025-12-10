# Bowler LL(1) Parser — Projeto de Compiladores

**Versão**: 2.0 | **Status**: ✅ Completo

## Quick Start

### Compilar
```bash
javac -d bin src/*.java  
```

### Executar um teste
```bash
java -cp bin Bowler src/teste_atribuicao.bw
```

### Gerar o txt
```bash
cd trabalho_compiladores/java
# compilar (somente se necessário)
javac -d bin src/*.java

# sobrescrever o arquivo de saída (limpa antes)
java -cp bin Bowler src/teste_atribuicao.bw > PROJETO_OUTPUT_teste_atribuicao.txt 2>&1

# ou anexar ao arquivo (mantém histórico)
java -cp bin Bowler src/teste_atribuicao.bw >> PROJETO_OUTPUT_teste_atribuicao.txt 2>&1

# ou ver no terminal e gravar ao mesmo tempo
java -cp bin Bowler src/teste_atribuicao.bw 2>&1 | tee PROJETO_OUTPUT_teste_atribuicao.txt

# ou capturar a sessão completa (inclui controle de terminal)
script -q -c "java -cp bin Bowler src/teste_atribuicao.bw" PROJETO_OUTPUT_teste_atribuicao.txt
```

### Saída esperada
```
✅ Parser Recursivo: Programa reconhecido sem erros.
✅ Análise SUCESSO! String aceita.
✅ Análise LL(1) com Pilha: ACEITA entrada lida
```

## O Que É Este Projeto?

Parser LL(1) completo para linguagem **Bowler**, incluindo:
- **Lexer**: tokenização com 18 tipos de tokens
- **Parser recursivo**: análise sintática com construção de AST
- **Análise LL(1)**: cálculo FIRST/FOLLOW e tabela M[A,a] sem conflitos
- **Parser LL(1) em pilha**: validação com algoritmo MATCH/EXPAND

## Novas Features (v2.0)

- ✅ **Tipo `string`**: `var nome: string = "Alice";`
- ✅ **Comparadores**: `>`, `<`, `==`, `!=`, `<=`, `>=`
- ✅ **Operadores lógicos**: `&&`, `||`
- ✅ **Else em if**: `if (c) { ... } else { ... }`
- ✅ **Return**: `return x;`

## Estrutura de Diretórios

```
src/
├── *.java (9 classes)
├── teste_*.bw (15 testes)
├── meu_exemplo.bw
└── meu_exemplo2.bw
bin/
└── *.class (compilados)
├── README.md (este arquivo)
├── LL1_ANALYSIS.md (análise técnica)
└── IMPLEMENTATION.md (detalhes de implementação)
```

## Como Compilar e Executar

Compilar uma vez:
```bash
javac -d bin src/*.java
```

Executar testes:
```bash
java -cp bin Bowler src/teste_string.bw
java -cp bin Bowler src/teste_comparadores.bw
java -cp bin Bowler src/teste_logicos.bw
java -cp bin Bowler src/teste_else.bw
java -cp bin Bowler src/teste_return.bw
```

## Capturar Output em Arquivo

Para gravar resultados completos em um arquivo `.txt`:

**Anexar ao arquivo** (ideal para múltiplos testes):
```bash
java -cp bin Bowler src/teste_atribuicao.bw >> OUTPUT.txt 2>&1
```

**Sobrescrever o arquivo**:
```bash
java -cp bin Bowler src/teste_atribuicao.bw > OUTPUT.txt 2>&1
```

**Ver no terminal e gravar ao mesmo tempo**:
```bash
java -cp bin Bowler src/teste_atribuicao.bw 2>&1 | tee OUTPUT.txt
```

Converter TXT para PDF (requer `pandoc`):
```bash
pandoc OUTPUT.txt -o OUTPUT.pdf
```

## Status Final

| Métrica | Valor |
|---|---|
| Código Java | 9 arquivos |
| Testes | 15 (100% passando) |
| Conflitos LL(1) | 0 |
| Documentação | 8 arquivos |
| Features | 5 novas |

## Documentação

- **LL1_ANALYSIS.md** — Análise técnica com FIRST/FOLLOW
- **IMPLEMENTATION.md** — Detalhes de cada classe
- **QUICKSTART.md** — Guia para começar
- **TESTES.md** — Descrição dos testes

## 📝 Exemplos de Uso

### String Type
```bowler
main {
  var msg: string = "Hello World";
  print(msg);
}
```

### Comparadores e Lógicos
```bowler
main {
  var x: int = 10;
  var y: int = 5;
  if (x > y && y != 0) {
    print(1);
  }
}
```

### Else
```bowler
main {
  var x: int = 5;
  if (x > 3) {
    print(1);
  } else {
    print(2);
  }
}
```

### Return
```bowler
main {
  var x: int = 42;
  return x;
}
```

