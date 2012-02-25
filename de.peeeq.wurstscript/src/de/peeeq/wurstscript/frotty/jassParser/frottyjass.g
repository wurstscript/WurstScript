grammar frottyjass;


@lexer::header {
  package de.peeeq.wurstscript.frotty.jassParser; 
}

@header {
  package de.peeeq.wurstscript.frotty.jassParser; 
  import de.peeeq.wurstscript.jassAst.*;
  import com.google.common.collect.Lists;
  import static de.peeeq.wurstscript.jassAst.JassAst.*;
  import de.peeeq.wurstscript.utils.*;
}

@members {
    private List<String> errors = new ArrayList<String>();
    public void displayRecognitionError(String[] tokenNames,
                                        RecognitionException e) {
        String hdr = getErrorHeader(e);
        String msg = getErrorMessage(e, tokenNames);
        errors.add(hdr + " " + msg);
        //throw new Error(e);
    }
    public List<String> getErrors() {
        return errors;
    }
}



// Globals


file returns [JassProg prog] : 
  {
    $prog = JassProg(JassVars(), JassFunctions());
  }
  NEWLINE? (file_body[$prog] NEWLINE)* function[$prog]* EOF
  ;

file_body[JassProg prog]: typeDefinitions | globalsBlock | nativeDeclaration
  ;
  
typeDefinitions
  : 'type' ID 'extends' ('handle'|ID)
  ;

globalsBlock
  : 'globals' NEWLINE global_variables 'endglobals'
  ;

global_variables
  : ( 'constant' type ID '=' expression NEWLINE
  | variable_declaration NEWLINE )*
  ;

nativeDeclaration
  : 'constant'? 'native' function_declaration
  ;
  

function[JassProg prog]
  : 'constant'? 'function' decl=function_declaration NEWLINE
    locals statements 'endfunction' NEWLINE
    {
      JassFunction f = JassFunction(decl.name, decl.params, decl.returnType, JassVars(), JassStatements());
      $prog.getFunctions().add(f);
    }
  ;

function_declaration returns [String name, JassSimpleVars params, String returnType]
  : n=ID
  {
    $name = n.getText();
  }
  
   'takes' 
   (
      'nothing' { $params = JassSimpleVars(); }
      | ps=paramaters {$params = ps; } 
   ) 
   'returns' 
   (
        rt=type { $returnType = rt; }
        | 'nothing' { $returnType = "nothing"; }
   )
  ;

paramaters returns [JassSimpleVars vars]
  : 
  {
    $vars = JassSimpleVars();
  }
  
  t1=type n1=ID { $vars.add(JassSimpleVar(t1, n1.getText())); }
  
   (',' t2=type n2=ID  { $vars.add(JassSimpleVar(t2, n2.getText())); })*
  ;
  
// Locals
locals
  : ('local' variable_declaration  NEWLINE)*
  ;

variable_declaration
  : type ID ('=' expression)? | type 'array' ID
  ;
//Statements
statements
  : (statement NEWLINE)*
  ;

statement
  : set | call | s_if | loop | exitwhen | s_return | debug
  ;

  set : 'set' ID '=' expression | 'set' ID '[' expression ']' '=' expression
    ;


  call  : 'call' ID '(' arguments? ')'
    ;

  s_if  : 'if' expression 'then' NEWLINE statements else_clause? 'endif'
    ;
  else_clause 
    : 'else' NEWLINE statements
    | 'elseif' expression 'then' NEWLINE statements else_clause?
    ;
    
  loop  : 'loop' NEWLINE statements 'endloop'
    ;

  exitwhen
    : 'exitwhen' expression //must be in a loop
    ;

  s_return  : 'return' expression?
    ;

  debug : 'debug' (set|call|s_if|loop)
    ;


//Expressions

expression returns [JassExpr jexpr] //?
  : andExpression | function_call | array_reference | func_reference | constant
  ;
  
andExpression returns [JassExpr jexpr]
  :   
  left=orExpression 
  {
    $jexpr = left;
  }
  (
    'and' right=orExpression
    {
      $jexpr = JassExprBinary($jexpr, JassOpAnd(), right); 
    }
  )*
  ;
  
orExpression returns [JassExpr jexpr]
  :   
  left=multExpression 
  {
    $jexpr = left;
  }
  (
    'or' right=multExpression
    {
      $jexpr = JassExprBinary($jexpr, JassOpOr(), right); 
    }
    
  )*
  ; 

multExpression returns [JassExpr jexpr]
    :   
    left = miniExpression 
    {
      $jexpr = left;
    }
    ( 
      '==' right=miniExpression 
      {
	      $jexpr = JassExprBinary($jexpr, JassOpEquals(), right); 
	    }      
      | '!=' right=miniExpression  
      {
        $jexpr = JassExprBinary($jexpr, JassOpUnequals(), right); 
      }   
      | '>' right=miniExpression
      {
        $jexpr = JassExprBinary($jexpr, JassOpGreater(), right); 
      } 
      | '<' right=miniExpression
      {
        $jexpr = JassExprBinary($jexpr, JassOpLess(), right); 
      } 
      | '<=' right=miniExpression
      {
        $jexpr = JassExprBinary($jexpr, JassOpLessEq(), right); 
      } 
      | '>=' right=miniExpression
      {
        $jexpr = JassExprBinary($jexpr, JassOpGreaterEq(), right); 
      } 
    )*
    ;

miniExpression returns [JassExpr jexpr] 
  : 
  left=mikroExpression
  {
	  $jexpr = left;
	}
  ( '+' right=mikroExpression 
  {
    $jexpr = JassExprBinary($jexpr, JassOpPlus(), right); 
  } 
  | '-' right=mikroExpression   
  {
    $jexpr = JassExprBinary($jexpr, JassOpMinus(), right); 
  }   
  )*
  ;

mikroExpression returns [JassExpr jexpr] 
  : 
  left=atom 
  {
    $jexpr = left;
  }
  ( '*' right=atom 
  {
    $jexpr = JassExprBinary($jexpr, JassOpMult(), right); 
  }   
  | '/' right=atom
  {
    $jexpr = JassExprBinary($jexpr, JassOpDiv(), right); 
  }  
  )*
  ; 

atom  returns [JassExpr jexpr]
    : 
    varac=ID
    {
      $jexpr = JassExprVarAccess( varac.getText() );
    }
    | '-' right=atom
    {
      $jexpr = JassExprUnary( JassOpMinus(), right );
    }
    | 'not' right=atom
    {
      $jexpr = JassExprUnary( JassOpNot(), right );
    }
    |   '(' expr=expression ')'
    {
      $jexpr = expr;
    }
    ;
    
function_call
  : 'call' ID '(' arguments ')'
  ;

arguments
  : ID // TODO
  ;

array_reference
  : ID '[' expression ']'
  ;

  func_reference
    : 'function' ID
    ;

  constant returns [JassExprAtomic jatomic]
    : 
      iconst=int_const
      {
        $jatomic = iconst; 
      } 
      | rconst=Real_const
      {
        $jatomic = JassExprRealVal(rconst.getText()); 
      } 
      | bconst=Bool_const
      {
        $jatomic = bconst.getText().equals("false") ? JassExprBoolVal(false) : JassExprBoolVal(true);
      } 
      | sconst=String_const
      {
        $jatomic = JassExprStringVal(sconst.getText()); 
      } 
      | 'null'
      {
        $jatomic = JassExprNull(); 
      }
    ;

  int_const returns [JassExprIntVal jint]
    : 
    d=Decimal
    {
      $jint = JassExprIntVal(Integer.parseInt(d.getText())); //? :D
    } 
    | o=Octal
    {
      $jint = JassExprIntVal(Integer.parseInt(d.getText(), 8)); //? :D
    } 
    | h=Hex
    {
      $jint = JassExprIntVal(Integer.parseInt(d.getText(), 6)); //? :D
    } 
    | f=Fourcc
    {
      $jint = JassExprIntVal(Utils.parseAsciiInt4(d.getText())); //? :D
    } 
    ;


    Decimal : ('1'..'9')('0'..'9')*
      ;
      
    Octal : '0'('0'..'7')*
      ;
      
    Hex : '$'('0'..'9'|'a'..'f'|'A'..'F')+ | '0'('x''X') ('0'..'9'|'a'..'f'|'A'..'F')+ 
      ;
      
    Fourcc  : '\'' . . . . '\''
      ;
      
  Real_const
    : ('0'..'9')+ '.' ('0'..'9')* | '.' ('0'..'9')+
    ;

  Bool_const
    : 'true' | 'false'
    ;

  String_const
    : '"' .* '"' // TODO escape \n \" ...
    ;

//  parenthesis
//    : '(' expression ')'
//    ;

// BaseRegex
type returns [String typeName]  : t=(ID | 'code' | 'handle' | 'integer' | 'real' | 'boolean' | 'string')
  {
    $typeName = t.getText();
  }
  ;
ID    : ('a'..'z'|'A'..'Z') (('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ('a'..'z'|'A'..'Z'|'0'..'9') )?;

NEWLINE : ('\r' | '\n')+ ;
      
//stat:   expression NEWLINE {System.out.println($expression.value);}
//    |   ID '=' expression NEWLINE
//        {memory.put($ID.text, new Integer($expression.value));}
//    |   NEWLINE
//    



WS  :   (' '|'\t')+ {skip();} ;








 
