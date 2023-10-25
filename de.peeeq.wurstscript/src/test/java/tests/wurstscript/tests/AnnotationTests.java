package tests.wurstscript.tests;

import org.testng.annotations.Test;

public class AnnotationTests extends WurstScriptTest {



    @Test
    public void knownAnnotation_warning() {
        testAssertOkLines(false,
                "package Test",
                "@annotation function annotation()",
                "@annotation function compilethyme()",
                "@compilethyme function foo()"
        );
    }

    @Test
    public void overloading_with_other_warning() {
        testAssertOkLines(false,
                "function blub takes nothing returns nothing",
                "endfunction",
                "package A",
                "@annotation function annotation()",
                "@annotation function blub()",
                "endpackage",
                "package Test",
                "import A",
                "@blub function foo()"
        );
    }

    @Test
    public void knownAnnotationParam_warning() {
        testAssertOkLines(false,
                "package Test",
                "@annotation function annotation()",
                "@annotation function blub(int _x, string _s)",
                "@blub(3, \"a\") function foo()"
        );
    }

    @Test
    public void unknownAnnotationParam() {
        testAssertOkLines(false,
                "package Test",
                "@blub(\"a\") function foo()"
        );
    }

    @Test
    public void knownAnnotationWrongParam_warning() {
        testAssertErrorsLines(false, "Expected int but found string.",
                "package Test",
                "@annotation function annotation()",
                "@annotation function blub(int _x, string _s)",
                "@blub(\"a\",3) function foo()"
        );
    }

    @Test
    public void annotationOverloading_warning() {
        testAssertOkLines(false,
                "package Test",
                "@annotation function annotation()",
                "@annotation function deprecated(string _s)",
                "@annotation function deprecated()",
                "@deprecated function foo()",
                "@deprecated(\"use something else\") function bar()"
        );
    }

    @Test
    public void unknown_annotation_warning() {
        testAssertErrorsLines(false, "Annotation compilethyme is not defined.",
                "package Test",
                "@compilethyme function foo()"
        );
    }

    @Test
    public void unknown_annotation_war_but_no_error() {
        testAssertOkLines(false,
                "package Test",
                "@compilethyme function foo()"
        );
    }


}
