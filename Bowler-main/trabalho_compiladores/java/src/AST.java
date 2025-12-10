import java.util.*;

public class AST {
    public static class InterpolatedString implements Expr {
        public final java.util.List<Object> parts; // String ou Expr

        public InterpolatedString(java.util.List<Object> parts) {
            this.parts = parts;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("InterpolatedString [");
            for (Object p : parts) {
                if (p instanceof String) {
                    indent(i + 1);
                    System.out.println("str: '" + p + "'");
                } else if (p instanceof Expr) {
                    indent(i + 1);
                    System.out.println("expr:");
                    ((Expr) p).prettyPrint(i + 2);
                }
            }
            indent(i);
            System.out.println("]");
        }

        public String toDetailedString() {
            StringBuilder sb = new StringBuilder();
            sb.append("InterpolatedString[");
            for (Object p : parts) {
                if (p instanceof String) {
                    sb.append("str:'").append(p).append("', ");
                } else if (p instanceof Expr) {
                    sb.append("expr:").append(((Expr) p).toDetailedString()).append(", ");
                }
            }
            if (!parts.isEmpty())
                sb.setLength(sb.length() - 2); // remove última vírgula
            sb.append("]");
            return sb.toString();
        }
    }

    public static class Ternary implements Expr {
        public final Expr cond;
        public final Expr thenExpr;
        public final Expr elseExpr;

        public Ternary(Expr cond, Expr thenExpr, Expr elseExpr) {
            this.cond = cond;
            this.thenExpr = thenExpr;
            this.elseExpr = elseExpr;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("Ternary");
            indent(i + 1);
            System.out.println("cond:");
            cond.prettyPrint(i + 2);
            indent(i + 1);
            System.out.println("then:");
            thenExpr.prettyPrint(i + 2);
            indent(i + 1);
            System.out.println("else:");
            elseExpr.prettyPrint(i + 2);
        }

        public String toDetailedString() {
            return "Ternary(cond:" + cond.toDetailedString() + ", then:" + thenExpr.toDetailedString() + ", else:"
                    + elseExpr.toDetailedString() + ")";
        }
    }

    public static class Print implements Statement {
        public final Expr value;

        public Print(Expr value) {
            this.value = value;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("Print");
            value.prettyPrint(i + 1);
        }

        public String toDetailedString() {
            return "Print(" + value.toDetailedString() + ")";
        }
    }

    public static class Input implements Expr {
        public final String prompt;

        public Input(String prompt) {
            this.prompt = prompt;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("Input(prompt='" + prompt + "')");
        }

        public String toDetailedString() {
            return "Input(prompt='" + prompt + "')";
        }
    }

    public static class Break implements Statement {
        public Break() {
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("Break");
        }

        public String toDetailedString() {
            return "Break";
        }
    }

    public static class Continue implements Statement {
        public Continue() {
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("Continue");
        }

        public String toDetailedString() {
            return "Continue";
        }
    }

    public static class Switch implements Statement {
        public final Expr expr;
        public final java.util.List<Case> cases;
        public final Block defaultBlock;

        public Switch(Expr expr, java.util.List<Case> cases, Block defaultBlock) {
            this.expr = expr;
            this.cases = cases;
            this.defaultBlock = defaultBlock;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("Switch");
            indent(i + 1);
            System.out.println("expr:");
            expr.prettyPrint(i + 2);
            for (Case c : cases)
                c.prettyPrint(i + 1);
            if (defaultBlock != null) {
                indent(i + 1);
                System.out.println("default:");
                defaultBlock.prettyPrint(i + 2);
            }
        }

        public String toDetailedString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Switch(expr:").append(expr.toDetailedString()).append(", cases:[");
            for (Case c : cases)
                sb.append(c.toDetailedString()).append(", ");
            if (!cases.isEmpty())
                sb.setLength(sb.length() - 2);
            sb.append("]");
            if (defaultBlock != null)
                sb.append(", default:").append(defaultBlock.toDetailedString());
            sb.append(")");
            return sb.toString();
        }
    }

    public static class Case {
        public final Expr value;
        public final Block block;

        public Case(Expr value, Block block) {
            this.value = value;
            this.block = block;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("case:");
            value.prettyPrint(i + 1);
            block.prettyPrint(i + 1);
        }

        public String toDetailedString() {
            return "Case(value:" + value.toDetailedString() + ", block:" + block.toDetailedString() + ")";
        }
    }

    public static class For implements Statement {
        public final Statement init;
        public final Expr cond;
        public final Expr inc;
        public final Block body;

        public For(Statement init, Expr cond, Expr inc, Block body) {
            this.init = init;
            this.cond = cond;
            this.inc = inc;
            this.body = body;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("For");
            if (init != null) {
                indent(i + 1);
                System.out.println("init:");
                init.prettyPrint(i + 2);
            }
            if (cond != null) {
                indent(i + 1);
                System.out.println("cond:");
                cond.prettyPrint(i + 2);
            }
            if (inc != null) {
                indent(i + 1);
                System.out.println("inc:");
                inc.prettyPrint(i + 2);
            }
            indent(i + 1);
            System.out.println("body:");
            body.prettyPrint(i + 2);
        }

        public String toDetailedString() {
            return "For(init:" + (init != null ? init.toDetailedString() : "null") + ", cond:"
                    + (cond != null ? cond.toDetailedString() : "null") + ", inc:"
                    + (inc != null ? inc.toDetailedString() : "null") + ", body:" + body.toDetailedString() + ")";
        }
    }

    // ...existing code...
    public static class ArrayLiteral implements Expr {
        public final java.util.List<Expr> elements;

        public ArrayLiteral(java.util.List<Expr> elements) {
            this.elements = elements;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("ArrayLiteral [");
            for (Expr e : elements)
                e.prettyPrint(i + 1);
            indent(i);
            System.out.println("]");
        }

        public String toDetailedString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ArrayLiteral[");
            for (Expr e : elements)
                sb.append(e.toDetailedString()).append(", ");
            if (!elements.isEmpty())
                sb.setLength(sb.length() - 2);
            sb.append("]");
            return sb.toString();
        }
    }

    public static class ArrayAccess implements Expr {
        public final Expr array;
        public final Expr index;

        public ArrayAccess(Expr array, Expr index) {
            this.array = array;
            this.index = index;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("ArrayAccess");
            array.prettyPrint(i + 1);
            index.prettyPrint(i + 1);
        }

        public String toDetailedString() {
            return "ArrayAccess(array:" + array.toDetailedString() + ", index:" + index.toDetailedString() + ")";
        }
    }

    // --- Nó de Tipo ---
    public static class TypeNode {
        public final String name;

        public TypeNode(String n) {
            name = n;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("Type(" + name + ")");
        }
    }

    // --- Programa ---
    public static class Program {
        public final Block mainBlock;

        public Program(Block b) {
            mainBlock = b;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("Program");
            indent(i + 1);
            System.out.println("main:");
            mainBlock.prettyPrint(i + 2);
        }

        public String toDetailedString() {
            return "Program(main:" + mainBlock.toDetailedString() + ")";
        }
    }

    // --- Bloco e Statements ---
    public interface Statement {
        void prettyPrint(int i);

        String toDetailedString();
    }

    public static class Block implements Statement {
        public final List<Statement> statements;

        public Block(List<Statement> stmts) {
            this.statements = stmts;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("Block {");
            for (Statement s : statements)
                s.prettyPrint(i + 1);
            indent(i);
            System.out.println("}");
        }

        public String toDetailedString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Block[");
            for (Statement s : statements)
                sb.append(s.toDetailedString()).append(", ");
            if (!statements.isEmpty())
                sb.setLength(sb.length() - 2);
            sb.append("]");
            return sb.toString();
        }
    }

    public static class VarDecl implements Statement {
        public final Token name;
        public final TypeNode type;
        public final Expr initializer;

        public VarDecl(Token n, TypeNode t, Expr init) {
            name = n;
            type = t;
            initializer = init;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("VarDecl " + name.lexeme + ":");
            type.prettyPrint(i + 1);
            if (initializer != null) {
                indent(i + 1);
                System.out.println("init:");
                initializer.prettyPrint(i + 2);
            }
        }

        public String toDetailedString() {
            return "VarDecl(name:" + name.lexeme + ", type:" + type.name
                    + (initializer != null ? ", init:" + initializer.toDetailedString() : "") + ")";
        }
    }

    public static class If implements Statement {
        public final Expr cond;
        public final Block thenB;
        public final Block elseB;

        public If(Expr c, Block t, Block e) {
            cond = c;
            thenB = t;
            elseB = e;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("If");
            indent(i + 1);
            System.out.println("cond:");
            cond.prettyPrint(i + 2);
            indent(i + 1);
            System.out.println("then:");
            thenB.prettyPrint(i + 2);
            if (elseB != null) {
                indent(i + 1);
                System.out.println("else:");
                elseB.prettyPrint(i + 2);
            }
        }

        public String toDetailedString() {
            return "If(cond:" + cond.toDetailedString() + ", then:" + thenB.toDetailedString()
                    + (elseB != null ? ", else:" + elseB.toDetailedString() : "") + ")";
        }
    }

    public static class While implements Statement {
        public final Expr cond;
        public final Block body;

        public While(Expr c, Block b) {
            cond = c;
            body = b;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("While");
            indent(i + 1);
            System.out.println("cond:");
            cond.prettyPrint(i + 2);
            indent(i + 1);
            System.out.println("body:");
            body.prettyPrint(i + 2);
        }

        public String toDetailedString() {
            return "While(cond:" + cond.toDetailedString() + ", body:" + body.toDetailedString() + ")";
        }
    }

    public static class DoWhile implements Statement {
        public final Block body;
        public final Expr cond;

        public DoWhile(Block b, Expr c) {
            body = b;
            cond = c;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("DoWhile");
            indent(i + 1);
            System.out.println("body:");
            body.prettyPrint(i + 2);
            indent(i + 1);
            System.out.println("cond:");
            cond.prettyPrint(i + 2);
        }

        public String toDetailedString() {
            return "DoWhile(body:" + body.toDetailedString() + ", cond:" + cond.toDetailedString() + ")";
        }
    }

    public static class Return implements Statement {
        public final Expr value;

        public Return(Expr v) {
            value = v;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("Return");
            if (value != null)
                value.prettyPrint(i + 1);
        }

        public String toDetailedString() {
            return "Return(" + (value != null ? value.toDetailedString() : "") + ")";
        }
    }

    public static class ExprStmt implements Statement {
        public final Expr expr;

        public ExprStmt(Expr e) {
            expr = e;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("ExprStmt");
            expr.prettyPrint(i + 1);
        }

        public String toDetailedString() {
            return "ExprStmt(" + expr.toDetailedString() + ")";
        }
    }

    // --- Expressões ---
    public interface Expr {
        void prettyPrint(int i);

        String toDetailedString();
    }

    public static class Literal implements Expr {
        public final Object value;

        public Literal(Object v) {
            value = v;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("Literal(" + value + ")");
        }

        public String toDetailedString() {
            return "Literal(" + value + ")";
        }
    }

    public static class Variable implements Expr {
        public final Token name;

        public Variable(Token n) {
            name = n;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("Var(" + name.lexeme + ")");
        }

        public String toDetailedString() {
            return "Var(" + name.lexeme + ")";
        }
    }

    public static class Grouping implements Expr {
        public final Expr expr;

        public Grouping(Expr e) {
            expr = e;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("Group(");
            expr.prettyPrint(i + 1);
            indent(i);
            System.out.println(")");
        }

        public String toDetailedString() {
            return "Group(" + expr.toDetailedString() + ")";
        }
    }

    public static class Unary implements Expr {
        public final Token op;
        public final Expr right;

        public Unary(Token o, Expr r) {
            op = o;
            right = r;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("Unary " + op.type);
            right.prettyPrint(i + 1);
        }

        public String toDetailedString() {
            return "Unary(" + op.type + ", " + right.toDetailedString() + ")";
        }
    }

    public static class Binary implements Expr {
        public final Expr left;
        public final Token op;
        public final Expr right;

        public Binary(Expr l, Token o, Expr r) {
            left = l;
            op = o;
            right = r;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("Binary " + op.type);
            left.prettyPrint(i + 1);
            right.prettyPrint(i + 1);
        }

        public String toDetailedString() {
            return "Binary(" + left.toDetailedString() + ", " + op.type + ", " + right.toDetailedString() + ")";
        }
    }

    public static class Assign implements Expr {
        public final Token name;
        public final Expr value;
        public final Token equals;

        public Assign(Token n, Expr v, Token e) {
            name = n;
            value = v;
            equals = e;
        }

        public void prettyPrint(int i) {
            indent(i);
            System.out.println("Assign " + name.lexeme);
            value.prettyPrint(i + 1);
        }

        public String toDetailedString() {
            return "Assign(" + name.lexeme + ", " + value.toDetailedString() + ")";
        }
    }

    // Utilidade de indentação
    public static void indent(int i) {
        for (int k = 0; k < i; k++)
            System.out.print("  ");
    }
}
