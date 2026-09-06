# Test patterns, bad and good

Code for each of the construction rules in `SKILL.md`. Examples are JUnit 5 unless stated;
remember that JUnit 4 takes the message as the **first** argument instead of the last.

Feedback messages are in English here. Write them in the language of `instructions.md`.

---

## 1. Two independent test functions per API function

Exercise: `static boolean equalArrays(char[] a1, char[] a2, int initialPos)`, recursive, `a1` and
`a2` have equal length.

### Bad - one test function for the whole API function

```java
@Test
@Timeout(1)
void testEqualArrays() {
    assertTrue(Main.equalArrays(new char[]{'a','b'}, new char[]{'a','b'}, 0));
    assertFalse(Main.equalArrays(new char[]{'a','b'}, new char[]{'a','c'}, 0));
    assertTrue(Main.equalArrays(new char[]{}, new char[]{}, 0));
    assertTrue(Main.equalArrays(new char[]{'a','b'}, new char[]{'z','b'}, 1));
}
```

A student who implemented everything but the empty-array case sees one red test and scores zero for
the function. The failure could be any of the four lines.

### Good - one test function per input case

```java
@Test @Order(1) @Timeout(1)
void test_001_equalArraysEmptyOrNull() { /* ... */ }

@Test @Order(2) @Timeout(1)
void test_002_equalArraysSingleElement() { /* ... */ }

@Test @Order(3) @Timeout(1)
void test_003_equalArraysSeveralElements() { /* ... */ }

@Test @Order(4) @Timeout(1)
void test_004_equalArraysInitialPosBeyondLength() { /* ... */ }
```

Partial credit becomes possible, the order is a study plan, and the method name already says which
case broke.

---

## 2. Two sub-cases with different expected values

### Bad - a constant passes

```java
@Test
void testDoorOpening() {
    Door door = new Door();
    door.open();
    assertTrue(door.isOpen(), "isOpen() incorrectly returned false");
}
```

`boolean isOpen() { return true; }` passes.

### Good - no constant satisfies both

```java
@Test
void test_001_doorOpenAndClose() {
    Door door = new Door();

    door.open();
    assertTrue(door.isOpen(), "isOpen() incorrectly returned false");

    door.close();
    assertFalse(door.isOpen(), "isOpen() incorrectly returned true");
}
```

The same applies to non-booleans. Two calls that must return *different* values, not two calls that
happen to return the same one.

Real example, from a bank account exercise:

```java
@Test(timeout = 1000)   // JUnit 4: message first
public void test_03_Levanta() {
    ContaBancaria conta = new ContaBancaria(100);
    assertTrue("levanta() should have returned true", conta.levanta(50));
    assertEquals("getSaldoComoString() returned the wrong value", "50", conta.getSaldoComoString());
    assertFalse("levanta() should have returned false", conta.levanta(70));
    assertEquals("after an invalid withdrawal the balance must not change", "50",
            conta.getSaldoComoString());
}
```

---

## 3. Messages that name the function

### Bad - no message

```java
assertEquals(5, Main.sum(1, 4));
```

Reports `expected: <5> but was: <0>`. Which function?

### Bad - the function name is not enough

```java
List<String> movieTitles = Main.getTitles();
boolean found = movieTitles.contains("The Matrix");
assertTrue(found, "getTitles()");
```

Reports `getTitles() ==> expected: <true> but was: <false>` about a function that returns a list.
Students read this as "getTitles() should return true" and get stuck.

### Good - say what is missing

```java
List<String> movieTitles = Main.getTitles();
String title = "The Matrix";
boolean found = movieTitles.contains(title);
assertTrue(found, "getTitles() missing title: " + title);
```

### Structured return values: point at the position

```java
// bad: reads as if getStatistics() returned an int
assertEquals(42, Main.getStatistics()[1], "getStatistics()");

// good
assertEquals(42, Main.getStatistics()[1], "getStatistics()[1]");
```

---

## 4. Tapering the feedback inside a test function

```java
@Test
@Timeout(1)
void test_004_sumArray() {
    String suffix = "returned an incorrect value.";

    // feedback includes the arguments (vulnerable to overfitting, but debuggable)
    assertEquals(5, Main.sum(new int[]{1, 4}), "sum({1, 4}) " + suffix);
    assertEquals(6, Main.sum(new int[]{2, 2, 2}), "sum({2, 2, 2}) " + suffix);

    // feedback is a hint, not the arguments
    assertEquals(-6, Main.sum(new int[]{-1, -2, -3}),
            "sum(...) " + suffix + " Consider arrays with negative numbers.");

    // feedback is the function name only
    assertEquals(9, Main.sum(new int[]{1, 2, 3, 4, -1}), "sum(...) " + suffix);
}
```

Why this works: the honest student fixes the first two sub-cases with the arguments in hand, and by
then understands the function well enough for the silent ones. The overfitting student learns two
input/output pairs and still fails the rest.

Here is what they would otherwise get away with, given only the first kind of message:

```java
static long sum(int n1, int n2) {
    if (n1 == 1) return 5;      // first assertion said sum(1, 4) == 5
    else if (n1 == 8) return 10; // second assertion said sum(8, 2) == 10
    return 0;
}
```

**Mini-tests and defenses**: make the *last* assertion of every test function silent.

---

## 5. Assert early and often

### Bad - one assertion after a sequence of actions

```java
Door door = new Door();
door.open();
door.close();
assertEquals("This door is closed!", door.toString());
```

If `open()` is what is broken, the message blames `toString()`.

### Good - assert after each action

```java
Door door = new Door();

door.open();
assertTrue(door.isOpen(), "isOpen() incorrectly returned false");
assertEquals("This door is open!", door.toString(), "toString() after open()");

door.close();
assertFalse(door.isOpen(), "isOpen() incorrectly returned true");
assertEquals("This door is closed!", door.toString(), "toString() after close()");
```

This matters most in OOP exercises, where state accumulates across calls.

---

## 6. Guard null references

### Bad - a NullPointerException with no context

```java
Actor actor = Main.getActorByID(1000);
assertEquals("Keanu Reeves", actor.getName(), "Actor getName()");
```

### Good

```java
Actor actor = Main.getActorByID(1000);
assertNotNull(actor, "getActorByID(1000) returned null");
assertEquals("Keanu Reeves", actor.getName(), "Actor getName()");
```

Apply it to every value the students' code returns before you call anything on it, including
elements pulled out of a returned collection.

---

## 7. Collections: elements before size

### Bad - the size first

```java
List<Integer> ids = Main.calculateIds();
assertEquals(347, ids.size(), "calculateIds() returned the wrong number of elements");
```

"You returned 340 instead of 347" leaves the student with no idea which are missing.

### Good - a few elements, each guarded, size last

```java
List<Integer> ids = Main.calculateIds();

int size = ids.size();
assertTrue(size >= 2,
        "calculateIds() should have returned at least 2 elements, but returned " + size);
assertEquals(3, ids.get(0), "calculateIds()[0]");
assertEquals(5, ids.get(1), "calculateIds()[1]");

assertTrue(size >= 5, "calculateIds() should have returned at least 5 elements");
assertEquals(6, ids.get(4), "calculateIds()[4]");

assertEquals(347, size, "calculateIds() returned the wrong number of elements");
```

The guard before each index is what turns an `IndexOutOfBoundsException` into a sentence.

For an unordered collection, check membership element by element instead of comparing the whole
structure, and name the missing element:

```java
for (String expectedTitle : expected) {
    assertTrue(obtained.contains(expectedTitle),
            "obterTitulosProdutos() did not return \"" + expectedTitle + "\"");
}
assertEquals(expected.size(), obtained.size(),
        "obterTitulosProdutos() returned extra elements");
```

---

## 8. Names, numbers and order

```java
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Timeout(1)
class TestTeacherStore {

    @Test @Order(1) void test_001_addProductToEmptyStore() { }
    @Test @Order(2) void test_002_addProductTwiceIsRejected() { }
    @Test @Order(3) void test_003_sellProductUntilOutOfStock() { }
}
```

JUnit 4 equivalent, where the name *is* the order:

```java
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestTeacher {
    @Test(timeout = 1000) public void test_01_Constructor() { }
    @Test(timeout = 1000) public void test_02_Deposita() { }
}
```

Keep `@Order(n)` and the number in the name in sync. They drift as tests are inserted, and then the
report order stops matching the numbers students quote when they ask for help.

---

## 9. Timeouts

```java
// JUnit 4, milliseconds
@Test(timeout = 500)

// JUnit 5, seconds by default, on the method or on the whole class
@Timeout(1)
@Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
```

Drop Project's validator reports an error for every test method without one. Beyond infinite loops,
a tight timeout is the only practical way a suite detects an O(n^2) solution where O(n) was asked
for - but only if the test input is large enough for the difference to show.

---

## 10. Progressive disclosure

```java
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Timeout(1)
class TestTeacher {

    static int passed = 0;

    @Order(1) @Test
    void test_001_addProduct() {
        // ... assertions ...
        passed++;          // last statement, so it only counts when the test passed
    }

    // ... tests 2 to 9, each ending in passed++ ...

    @Order(12) @Test
    void test_012_addDuplicateProducts() {
        if (passed < 9) {
            fail("This test only runs once tests 1 to 9 pass");
        }
        // ... the actual test ...
    }
}
```

Requirements:

- **Only in in-class exercises, homework and projects.** Never in a mini-test or a defense. Gating
  assumes the student can come back and unlock the rest; in a time-bound assessment they cannot, so
  one early mistake they run out of time to fix silently zeroes every gated test after it.
- `passed++` must be the **last** statement of the test, after every assertion.
- The gated test must have a higher `@Order` than the tests that unlock it, otherwise it runs first
  and always fails.
- Say so in `instructions.md`: which tests are gated, and what unlocks them.

This is deliberately against JUnit's independence philosophy. It is worth it when a suite has a
long tail of advanced tests that would otherwise fill a beginner's report with noise.

---

## 11. Non-functional requirements

### Forbidden constructs, through checkstyle

In `checkstyle.xml`, inside `<module name="TreeWalker">`. The `message` is what the student sees:

```xml
<module name="RegexpSinglelineJava">
    <property name="format" value="System\.exit"/>
    <property name="ignoreComments" value="true"/>
    <property name="message" value="System.exit() is not allowed. Throw an exception, or handle the error gracefully."/>
</module>

<module name="RegexpSinglelineJava">
    <property name="format" value="instanceof"/>
    <property name="ignoreComments" value="true"/>
    <property name="message" value="instanceof is not allowed. Use polymorphism."/>
</module>
```

Typical uses: forcing recursion by forbidding `for` and `while`, forcing polymorphism by forbidding
`instanceof` and `getClass()`, forbidding `ordinal()` on enums.

### Structural requirements, through reflection

That a method exists, with the required signature:

```java
@Test
@Timeout(1)
void test_002_methodEstaPrestesAEsgotarExists() {
    Method method = null;
    try {
        method = Produto.class.getDeclaredMethod("estaPrestesAEsgotar");
    } catch (NoSuchMethodException e) {
        fail("There is no estaPrestesAEsgotar() method in class pt.ulusofona.lp2.Produto");
    }
    assertEquals(boolean.class, method.getReturnType(),
            "estaPrestesAEsgotar() must return boolean");
    assertEquals(0, method.getParameterCount(),
            "estaPrestesAEsgotar() must take no parameters");
}
```

Always state the full expected signature in the failure message. `NoSuchMethodException` alone tells
the student nothing about what was looked for.

Reflection also covers requirements that no assertion reaches: `Modifier.isAbstract(clazz
.getModifiers())` for "this class must be abstract", `clazz.getSuperclass()` for a required
hierarchy, `Modifier.isPrivate(field.getModifiers())` for encapsulation.

Reflection can go further, and let students choose their own function names while still being
tested:

```java
// finds the single static method in Main whose name starts with "f5_", whatever it is called
Method f5 = ReflectionUtils.findStaticMethodStartingWith(Main.class, "f5_");
assertEquals(12L, ReflectionUtils.invokeStatic(f5, (Object) new int[]{3, 4, 5}));
```

with the helper failing loudly when there is no such method, or more than one. This is worth the
trouble in exercises whose learning objective includes naming.

---

## Kotlin

Same rules, JUnit 5 annotations, `kotlin.test` assertions:

```kotlin
import org.junit.jupiter.api.*
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@TestMethodOrder(OrderAnnotation::class)
class TestTeacher {

    @Test
    @Order(1)
    @Timeout(500, unit = TimeUnit.MILLISECONDS)
    fun test_001_f1() {
        val filmes = listOf(
            Filme("Indiana Jones", TipoFilme.AVENTURA, 1981, 5, 120),
            Filme("Star Wars", TipoFilme.AVENTURA, 1977, 6, 150)
        )
        assertEquals(listOf("Star Wars"), f1(filmes), "f1() - consider the type and the duration")
    }
}
```

Set `language: KOTLIN` on the assignment. Kotlin projects use detekt instead of checkstyle, so the
forbidden-construct rules above move to `detekt-config.yml`.
