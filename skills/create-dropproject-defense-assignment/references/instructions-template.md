# The defense's instructions.md

The instructions are the exam paper. They are hidden from students until the teacher releases them,
so they are read for the first time under a clock, by someone who is nervous. Write for that reader:
short sentences, exact signatures, worked examples for anything with a format.

## Structure

```markdown
# <Defense of <course>, <exam period> - v<n>>

### Notas prévias        <- conduct rules
### Duração              <- the clock
### Introdução           <- what this is, how many changes, how many tests, what must not break
### Instruções           <- the numbered changes
```

### Notas prévias

The rules of conduct. Reuse the course's own wording verbatim from the previous edition rather than
writing new ones - students recognise them, and they usually carry a stated penalty that the teacher
has already announced. What they cover:

- what may be consulted: own notes, own weekly exercise code, the course's Moodle, and - **only if
  the teacher confirms it for this defense** - the open internet. This line varies: some defenses
  allow Google and Stack Overflow, others restrict students to Moodle and nothing else. Ask every
  time, and do not carry the previous edition's wording over on this one line - the wider rule is
  not the safe default
- what may not: generative AI sites, AI plugins such as Copilot, any other human being, messaging
  apps
- proctoring: stay on the call for the whole session, even after finishing
- the code-quality rules that carry a grade penalty, with the number of marks stated

### Duração

The number of minutes. On its own line, so nobody misses it.

### Introdução

Three things, in this order:

1. how many changes this defense asks for, and that they are changes to **their own project code**
2. **how many tests those changes translate into.** Keep this number true when tests are added or
   removed - students use it to check they have not missed a change.
3. that the changes must extend the program's behaviour **without breaking what was already there**.
   This is a grading criterion, so state it, in the teacher's words. When the original tests were
   kept alongside the defense tests - the usual case when defending a weekly assignment - say that
   too, and that they must still pass.

### Instruções

A numbered list, one item per change, in the order the tests report them.

**Item 1 is `AUTHORS.txt` when the assignment was done in groups**: a project made in pairs is
defended individually, so the file has to be cut down to the student's own number and name. Defending
a weekly assignment or mini-ficha, which was individual to begin with, there is nothing to cut -
leave the item out and start at the first real change.

For an **unlinked defense**, items 2 to 4 are the setup the linked flow does automatically: create a
project, use the same package, keep the class names the project required, stub out every required
method so it compiles. Then the exercises.

Each change should give:

- the exact signature, in backticks, when a function is added or changed
- the exact output format, with a worked example, when a format is involved
- the boundary conditions in words: inclusive or exclusive, what an empty result is, what happens
  when the thing does not exist
- what stays the same, explicitly, when the change only applies to a subset
- any simplifying assumption that keeps the change from becoming a puzzle, e.g. "you may assume the
  boards used in the tests for this change have no abysses or tools"

## Never in the instructions

- **The line budget.** Not the number, not the fact that a limit exists. Drop Project deliberately
  hides `maxChangedLines`; a budget students can see becomes a target to work up to instead of a
  limit that catches rewrites. They only ever meet it as a rejection message, and only if they trip
  it.
- The number of lines your reference solution changes, which is the same mistake wearing a hat.
- Anything that differs between versions other than the exercises themselves. Same conduct rules,
  same duration, same test count.
- Hints about which project functions the tests call internally. That is a map to the minimum edit.

## A worked example

`defesa-v1` of LP2 2025/26, abridged. The original is in Portuguese, which is the language of that
course's `instructions.md`; write yours in whatever language the students are taught in.

Two lines in it are specific to that defense and must not be copied blindly:

- `Pode consultar o Moodle ou qualquer site da Internet` - confirm with the teacher. When only Moodle
  is allowed it becomes something like `Pode consultar o Moodle. Não pode consultar qualquer outro
  site da Internet.`
- instruction 1, the `AUTHORS.txt` cut-down, is there only because that project was done in pairs.
  Defending something that was already individual, the numbering starts at the first real change.

```markdown
# Defesa do projeto de Linguagens de Programação II (época normal - v1)

### Notas prévias

* Pode consultar os seus próprios apontamentos, assim como o código das fichas práticas semanais que implementou
* Pode consultar o Moodle ou qualquer site da Internet (Google, Stackoverflow, etc.).
* Não é permitido o uso de qualquer site de IA Generativa (p.e. o ChatGPT, Bing Chat, Bard, Gemini ou similar).
* Não é permitida a utilização do GitHub Copilot ou qualquer outro plugin de IA Generativa.
* Não pode consultar (por qualquer via) outro ser humano.
* Durante a defesa não pode aceder a aplicações que permitam envio e recepção de mensagens (p.e. Discord, Whatsapp, etc)
* Deve manter-se ligado no zoom durante toda a prova (mesmo que termine antes do tempo).
* Deve seguir as boas práticas de programação por objetos, nomeadamente não utilizar o instanceof ou o getClass().
**Submissões com erros de qualidade de código terão uma penalização de 5 valores**

### Duração

60 minutos

### Introdução

A defesa consiste em 5 alterações muito simples que terá que fazer ao código do seu projeto. Estas
alterações traduzem-se em 8 testes. As suas alterações devem estender o comportamento do programa
sem estragar o mesmo, ou seja, não deve introduzir alterações que quebrem os requisitos originais.

### Instruções

1. Comece por alterar o `AUTHORS.txt` para incluir apenas o seu número e nome de aluno, pois a
   submissão é individual.
1. Adicione à sua classe `GameManager` uma nova função com o seguinte protocolo:
   `public List<String> getProgrammersBetweenPositions(int posicao1, int posicao2)`.

    Esta função deve devolver uma lista de Strings contendo informação sobre os programadores que se
    encontrem entre as posições `posicao1` e `posicao2` (ambas inclusive).
    Se não existirem programadores entre as posições indicadas, deve retornar uma lista vazia.
    As Strings devem respeitar a seguinte sintaxe: `Nome : Posição`.
    A ordem dos elementos na lista não é relevante.
    Pode assumir que todos os programmers estão "Em Jogo" quando a função for chamada.
1. Adicione suporte para uma nova **ferramenta** chamada `Martelo Dourado`, que deve ter as
   seguintes características:
    1. ID do Tipo = 100
    1. Previne os efeitos do abismo: `Exception`
    1. Apenas tem efeito se o programador usar a linguagem C
1. Altere o `getSlotInfo(...)` de forma a que, além da informação para abismos ("A: X") e
   ferramentas ("T: X"), passe a retornar `"V:Y"` para os slots vazios (isto é, sem abismos nem
   ferramentas), em que Y é o número de programadores nesse slot.
1. Os programadores cuja cor seja `Green` ou `Purple` não se podem mover para as casas que sejam
   múltiplos de 5. Os comportamentos dos restantes programadores devem permanecer iguais ao
   enunciado do projeto. As restrições de movimento devido às linguagens de programação devem-se
   manter para todas as cores.
1. Altere as condições de fim de jogo de tal forma que o vencedor seja o primeiro Programador a
   passar da primeira metade do tabuleiro. Pode assumir que os tabuleiros usados nos testes desta
   alteração não têm abismos nem ferramentas.
   Exemplos:
    1. Se o tabuleiro tem tamanho 10, um programador que calhe (ou ultrapasse) na casa 6 ganha
       imediatamente o jogo.
    2. Se o tabuleiro tem tamanho 9, um programador que calhe (ou ultrapasse) na casa 5 ganha
       imediatamente o jogo.
```

Worth noticing in it:

- every added function is given by its full signature
- every format is given literally, `Nome : Posição`, `"V:Y"`, `"A: X"`
- the boundaries are in words: "ambas inclusive", "deve retornar uma lista vazia", "a ordem não é
  relevante"
- change 5 says what does *not* change, twice: the other colours behave as before, and the language
  restrictions still apply
- change 6 hands the student an assumption that removes an interaction they would otherwise have to
  reason about, plus two worked examples for an off-by-one that would otherwise eat the hour
- the line budget appears nowhere
