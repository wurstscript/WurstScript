<!-- WURST_LANGUAGE_AGENT_DOC_VERSION: 2026-08-08 -->
# WurstScript language digest

This is the compact, agent-oriented language reference shipped with the WurstScript compiler. It covers language semantics and compiler-facing syntax; standard-library APIs, dependency conventions, UI rules, and object-editor policies belong to the project or dependency documentation.

## File and block structure

Every Wurst source file is inside a package. Blocks are indentation-based; use tabs or four spaces consistently and never mix indentation styles.

```wurst
package Example

init
	print("loaded")
```

Statements normally end at a newline. A newline can continue after `(`, `[`, or an operator, and before `.`, `..`, `)`, `]`, or `begin`.

## Declarations and expressions

Use `let` for immutable locals and `var` when mutation is required. Type inference is preferred when the type is clear. Explicit types remain useful at public boundaries and for lambda target types.

```wurst
let immutable = 5
var mutable = 10
constant int SOME_ID = 'A000'
int array values = [1, 2, 3]

function max(int a, int b) returns int
	if a > b
		return a
	return b
```

Primitive types include `boolean`, `int`, `real`, and `string`; Warcraft values usually use nullable handle types such as `unit`, `group`, `effect`, and `player`.

Operators include arithmetic (`+`, `-`, `*`, `/`), integer division (`div`), modulo (`%`, `mod`), boolean operators (`and`, `or`, `not`), comparisons, and the conditional expression `condition ? ifTrue : ifFalse`.

`/` is real division even when both operands are integers. Use `div` for integer division. WC3 integers are signed 32-bit and overflow silently, so convert before a large multiplication: `worth.toReal() * count`, never `(worth * count).toReal()`.

Control flow uses `if`/`else if`/`else`, `switch`/`case`/`default`, `while`, and `for`:

```wurst
for i = 0 to 10
	...
for i = 10 downto 0
	...
for unit u in group
	...
```

`continue` skips the current loop iteration. `skip` is a no-op statement.

## Null-safe access

`?.` accesses a member only when its receiver is non-null. The receiver is evaluated once, and method arguments are not evaluated when it is null.

```wurst
target?.kill()
let owner = target?.getOwner()
if node?.next?.next == null
	...
```

The receiver must have a nullable type; `int`, `real`, and `boolean` cannot use `?.`. If the accessed member returns a non-nullable value such as `int`, a null-safe call can only be used as a standalone statement. `?.` is not an assignment target.

## Functions, packages, and imports

Functions omit Jass-style `takes` and `returns nothing`:

```wurst
function printMax(int a, int b)
	print(max(a, b).toString())
```

Package members are private by default; use `public` for exports. Class members are public by default; use `private` or `protected` to restrict them. Every package implicitly imports `Wurst` unless it imports `NoWurst`.

`import` makes names available locally. `import public` also re-exports those names. Package initialization runs top-to-bottom, with imported packages initialized before their importers. Avoid `initlater` except to break an unavoidable initialization cycle.

Use `UpperCamelCase` for packages and classes, `lowerCamelCase` for functions, members, locals, and tuples, and `UPPER_SNAKE_CASE` for top-level constants.

## Cascade and extension syntax

The cascade operator calls methods on the same receiver and returns that receiver, which is useful for setup:

```wurst
CreateTrigger()
	..registerAnyUnitEvent(EVENT_PLAYER_UNIT_ISSUED_ORDER)
	..addCondition(Condition(function condition))
	..addAction(function action)
```

Extension functions use `this` as their receiver:

```wurst
public function unit.getX2() returns real
	return GetUnitX(this)
```

Prefer extension APIs and value tuples such as `vec2` over raw handle plumbing when the standard library provides them. Avoid unchecked `castTo`; prefer interfaces, modules, or explicit data modeling.

## Lambdas and closures

Every lambda needs a target type; standalone lambda expressions cannot infer one:

```wurst
Predicate<int> even = x -> x mod 2 == 0

doAfter(1.) ->
	print("later")
```

Locals captured by a closure are captured by value. Assigning to a captured local inside a callback does not update the outer local afterwards. Keep dependent work inside the callback that creates the value, store shared mutable state in an owning class, or use `reference(value)` deliberately and destroy the reference when finished.

Lambdas used as the Jass `code` type cannot accept parameters or capture locals.

## Classes, interfaces, modules, and tuples

Objects created with `new` generally need `destroy`; tuples are value types and must not be destroyed. Destructors (`ondestroy`) remain explicit for Lua output—Lua garbage collection does not replace Wurst ownership and cleanup.

```wurst
class Missile
	function onCollide(unit target)

class Fireball extends Missile
	override function onCollide(unit target)
		...
```

`super(...)` must be the first constructor statement. Overridden methods require `override`. Interfaces declare required methods; modules (`use`) inject reusable members.

Prefer `T:` generics for performance-sensitive or instance-heavy containers:

```wurst
class Box<T:>
	T value
```

The older unconstrained `T` form erases through integer casts and can share storage in surprising ways.

## Lua and Jass targets

The target is selected by the project `wurst.build` `scriptMode` field. `wc3Patch` separately selects the compatible core Jass and standard-library era.

Lua has no practical Jass operation limit; do not add `execute()` as a workaround. Use timers only for actual asynchronous delay. Jass has an operation limit per thread; `execute()` starts a new thread and heavy work may need chunking across ticks.

## Compiletime

Compiletime functions run while building the map and can generate object-editor data or constants:

```wurst
let value = compiletime(factorial(5))

@compiletime function createSpell()
	new AbilityDefinitionMountainKingThunderBolt(SPELL_ID)
		..setName("Wurst Bolt")
```

Use stable ID helpers and wrappers. Generated object definitions should use real melee objects as bases, not other custom generated objects; inherited object fields must be audited by the project’s object-data guidance.

## Formatting and diagnostics

Use spaces around binary operators, no space before call parentheses, and no spaces around `.`, `..`, or `?.`. Put doc comments (`/** ... */`) on public APIs when they should appear in autocomplete. Prefix intentionally unused variables with `_`.

When unsure, search the compiler’s tests and nearby working code. A successful parse is not proof of correct Wurst semantics: check ownership, closure capture, target mode, and the generated behavior as well.
