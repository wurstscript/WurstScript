grammar Llvm;
// based ON HTTPS://GIST.GITHUB.COM/MEWMEW/a2487392d5519ef49658fd8f84d9eed5


@header {
    package de.peeeq.wurstscript.llvm.antlr;
}

// ### ( syntax PART )? #########################################################

// the lLVM iR GRAMMAR HAS BEEN BASED ON THE SOURCE CODE OF THE OFFICIAL lLVM
// PROJECT, AS OF 2018-02-19 (REV db070bbdacd303ae7da129f59beaf35024d94c53).
//
//    * LIB/asmParser/lLParser.CPP

// === ( module )? ==============================================================

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#MODULE-STRUCTURE

// REF: run
//
//   MODULE ::= TOPLEVELENTITY*

module
	: topLevelEntity*
;


// --- ( top-LEVEL entities )? --------------------------------------------------

// REF: parseTopLevelEntities

topLevelEntity
	: sourceFilename
	| targetDefinition
	| moduleAsm
	| typeDef
	| comdatDef
	| globalDecl
	| globalDef
	| indirectSymbolDef
	| functionDecl
	| functionDef
	| attrGroupDef
	| namedMetadataDef
	| metadataDef
	| useListOrder
	| useListOrderBB
;

// ~~~ ( source filename )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#SOURCE-FILENAME

// REF: parseSourceFileName
//
//   ::= 'SOURCE_FILENAME' '=' sTRINGCONSTANT

sourceFilename
	: 'source_filename' '=' stringLit
;

// ~~~ ( target definition )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#TARGET-TRIPLE
// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DATA-LAYOUT

// REF: parseTargetDefinition
//
//   ::= 'TARGET' 'TRIPLE' '=' sTRINGCONSTANT
//   ::= 'TARGET' 'DATALAYOUT' '=' sTRINGCONSTANT

targetDefinition
	: 'target' 'datalayout' '=' stringLit
	| 'target' 'triple' '=' stringLit
;

// ~~~ ( module-LEVEL inline assembly )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#MODULE-LEVEL-INLINE-ASSEMBLY

// REF: parseModuleAsm
//
//   ::= 'MODULE' 'ASM' sTRINGCONSTANT

moduleAsm
	: 'module' 'asm' stringLit
;

// ~~~ ( type defintion )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#STRUCTURE-TYPE

// REF: parseUnnamedType
//
//   ::= localVarID '=' 'TYPE' TYPE

// REF: parseNamedType
//
//   ::= localVar '=' 'TYPE' TYPE

typeDef
	: localIdent '=' 'type' opaqueType
	| localIdent '=' 'type' type
;

// ~~~ ( comdat definition )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#LANGREF-COMDATS

// REF: PARSEcomdat

comdatDef
	: comdatName '=' 'comdat' selectionKind
;

selectionKind
	: 'any'
	| 'exactmatch'
	| 'largest'
	| 'noduplicates'
	| 'samesize'
;

// ~~~ ( global variable declaration )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#GLOBAL-VARIABLES

// REF: parseUnnamedGlobal
//
//   optionalVisibility (aLIAS | iFUNC) ...
//   optionalLinkage optionalPreemptionSpecifier optionalVisibility
//   optionalDLLStorageClass
//                                                     ...   -> GLOBAL VARIABLE
//   globalID '=' optionalVisibility (aLIAS | iFUNC) ...
//   globalID '=' optionalLinkage optionalPreemptionSpecifier optionalVisibility
//                optionalDLLStorageClass
//                                                     ...   -> GLOBAL VARIABLE

// REF: parseNamedGlobal
//
//   globalVar '=' optionalVisibility (aLIAS | iFUNC) ...
//   globalVar '=' optionalLinkage optionalPreemptionSpecifier
//                 optionalVisibility optionalDLLStorageClass
//                                                     ...   -> GLOBAL VARIABLE

// REF: parseGlobal
//
//   ::= globalVar '=' optionalLinkage optionalPreemptionSpecifier
//       optionalVisibility optionalDLLStorageClass
//       optionalThreadLocal optionalUnnamedAddr optionalAddrSpace
//       optionalExternallyInitialized globalType type const optionalAttrs
//   ::= optionalLinkage optionalPreemptionSpecifier optionalVisibility
//       optionalDLLStorageClass optionalThreadLocal optionalUnnamedAddr
//       optionalAddrSpace optionalExternallyInitialized globalType type
//       const optionalAttrs

globalDecl
	: globalIdent '=' externLinkage optPreemptionSpecifier optVisibility optDLLStorageClass optThreadLocal optUnnamedAddr optAddrSpace optExternallyInitialized immutable type globalAttrs funcAttrs
;

// ~~~ ( global variable definition )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

globalDef
	: globalIdent '=' optLinkage optPreemptionSpecifier optVisibility optDLLStorageClass optThreadLocal optUnnamedAddr optAddrSpace optExternallyInitialized immutable type constant globalAttrs funcAttrs
;

optExternallyInitialized
	: 
	| 'externally_initialized'
;

// REF: parseGlobalType
//
//   ::= 'CONSTANT'
//   ::= 'GLOBAL'

immutable
	: 'constant'
	| 'global'
;

globalAttrs
	: 
	| ',' globalAttrList
;

globalAttrList
	: globalAttr
	| globalAttrList ',' globalAttr
;

globalAttr
	: section
	| comdat
	| alignment
	//   ::= !DBG !57
	| metadataAttachment
;

// ~~~ ( indirect symbol definition )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#ALIASES
// HTTPS://LLVM.ORG/DOCS/langRef.HTML#IFUNCS

// REF: PARSEindirectSymbol
//
//   ::= globalVar '=' optionalLinkage optionalPreemptionSpecifier
//                     optionalVisibility optionalDLLStorageClass
//                     optionalThreadLocal optionalUnnamedAddr
//                     'ALIAS|IFUNC' indirectSymbol
//
//  indirectSymbol
//   ::= typeAndValue

indirectSymbolDef
	: globalIdent '=' externLinkage optPreemptionSpecifier optVisibility optDLLStorageClass optThreadLocal optUnnamedAddr alias type ',' type constant
	| globalIdent '=' optLinkage optPreemptionSpecifier optVisibility optDLLStorageClass optThreadLocal optUnnamedAddr alias type ',' type constant
;

alias
	: 'alias'
	| 'ifunc'
;

// ~~~ ( function declaration )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#FUNCTIONS

// REF: parseDeclare
//
//   ::= 'DECLARE' functionHeader

functionDecl
	: 'declare' metadataAttachments optExternLinkage functionHeader
;

// ~~~ ( function definition )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#FUNCTIONS

// REF: parseDefine
//
//   ::= 'DEFINE' functionHeader (!DBG !56)* '{' ...

functionDef
	: 'define' optLinkage functionHeader metadataAttachments functionBody
;

// REF: parseFunctionHeader
//
//   ::= optionalLinkage optionalPreemptionSpecifier optionalVisibility
//       optionalCallingConv optRetAttrs optUnnamedAddr type globalName
//       '(' argList ')' optFuncAttrs optSection optionalAlign optGC
//       optionalPrefix optionalPrologue optPersonalityFn

// tODO: add optAlignment BEFORE optGC ONCE THE lR-1 CONFLICT HAS BEEN RESOLVED,
// AS funcAttrs ALSO CONTAINS 'align'.

functionHeader
	: optPreemptionSpecifier optVisibility optDLLStorageClass optCallingConv returnAttrs type globalIdent '(' params ')' optUnnamedAddr funcAttrs optSection optComdat optGC optPrefix optPrologue optPersonality
;

optGC
	: 
	| 'gc' stringLit
;

optPrefix
	: 
	| 'prefix' type constant
;

optPrologue
	: 
	| 'prologue' type constant
;

optPersonality
	: 
	| 'personality' type constant
;

// REF: parseFunctionBody
//
//   ::= '(' basicBlock+ useListOrderDirective* ')*'

functionBody
	: '{' basicBlockList useListOrders '}'
;

// ~~~ ( attribute group definition )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#ATTRIBUTE-GROUPS

// REF: parseUnnamedAttrGrp
//
//   ::= 'ATTRIBUTES' attrGrpID '=' '(' attrValPair+ ')*'

attrGroupDef
	: 'attributes' attrGroupID '=' '{' funcAttrs '}'
;

// ~~~ ( named metadata definition )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#NAMED-METADATA

// REF: parseNamedMetadata
//
//   !FOO = !( !1, !2 )*

namedMetadataDef
	: metadataName '=' '!' '{' metadataNodes '}'
;

metadataNodes
	: 
	| metadataNodeList
;

metadataNodeList
	: metadataNode
	| metadataNodeList ',' metadataNode
;

metadataNode
	: metadataID
	// parse dIExpressions INLINE AS a SPECIAL CASE. they ARE STILL mDNodes, SO
	// THEY CAN STILL APPEAR IN NAMED METADATA. remove THIS LOGIC IF THEY BECOME
	// PLAIN metadata.
	| dIExpression
;

// ~~~ ( metadata definition )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#METADATA-NODES-AND-METADATA-STRINGS

// REF: parseStandaloneMetadata
//
//   !42 = !(...)*

metadataDef
	: metadataID '=' optDistinct mDTuple
	| metadataID '=' optDistinct specializedMDNode
;

optDistinct
	: 
	| 'distinct'
;

// ~~~ ( use-LIST order directives )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#USE-LIST-ORDER-DIRECTIVES

// REF: parseUseListOrder
//
//   ::= 'USELISTORDER' type value ',' useListOrderIndexes
//  useListOrderIndexes
//   ::= '(' uint32 (',' uint32)+ ')*'

useListOrders
	: 
	| useListOrderList
;

useListOrderList
	: useListOrder
	| useListOrderList useListOrder
;

useListOrder
	: 'uselistorder' type value ',' '{' indexList '}'
;

// REF: parseUseListOrderBB
//
//   ::= 'USELISTORDER_BB' @FOO ',' %BAR ',' useListOrderIndexes

useListOrderBB
	: 'uselistorder_bb' globalIdent ',' localIdent ',' '{' indexList '}'
;

// === ( identifiers )? =========================================================

// --- ( global identifiers )? --------------------------------------------------

globalIdent
	: GLOBAL_IDENT
;

// --- ( local identifiers )? ---------------------------------------------------

localIdent
	: LOCAL_IDENT
;

// --- ( label identifiers )? ---------------------------------------------------

labelIdent
	: LABEL_IDENT
;

// --- ( attribute group identifiers )? -----------------------------------------

attrGroupID
	: ATTR_GROUP_ID
;

// --- ( comdat identifiers )? --------------------------------------------------

comdatName
	: COMDAT_NAME
;

// --- ( metadata identifiers )? ------------------------------------------------

metadataName
	: METADATA_NAME
;

metadataID
	: METADATA_ID
;

// === ( types )? ===============================================================

// REF: parseType
//
//  tYPEKEYWORD('void',      type::GETvoidTy(context));
//  tYPEKEYWORD('half',      type::GEThalfTy(context));
//  tYPEKEYWORD('float',     type::GETfloatTy(context));
//  tYPEKEYWORD('double',    type::GETdoubleTy(context));
//  tYPEKEYWORD('x86_fp80',  type::getX86_fP80ty(context));
//  tYPEKEYWORD('fp128',     type::GETfP128ty(context));
//  tYPEKEYWORD('ppc_fp128', type::GETpPC_FP128ty(context));
//  tYPEKEYWORD('label',     type::GETlabelTy(context));
//  tYPEKEYWORD('metadata',  type::GETmetadataTy(context));
//  tYPEKEYWORD('x86_mmx',   type::getX86_mMXTy(context));
//  tYPEKEYWORD('token',     type::GETtokenTy(context));

type
	: voidType
	// types '(' argTypeListI ')' optFuncAttrs
	// funcType
	| type '(' params ')'
	// firstClassType
	// concreteType
    | metadataType
    | intType
	// type ::= 'FLOAT' | 'VOID' (ETC)
	| floatType
	// type ::= type '*'
	// type ::= type 'ADDRSPACE' '(' uint32 ')' '*'
	// pointerType
    | type optAddrSpace pointertype='*'
	// type ::= '<' ... '>'
	| vectorType
	| labelType
	// type ::= '(' ... ')?'
	| arrayType
	// type ::= structType
	| structType
	// type ::= %FOO
	// type ::= %4
	| namedType
	| mMXType
	| tokenType
;

concreteType:
      metadataType
    | intType
	// type ::= 'FLOAT' | 'VOID' (ETC)
	| floatType
	// type ::= type '*'
	// type ::= type 'ADDRSPACE' '(' uint32 ')' '*'
	// pointerType
	| type optAddrSpace pointertype='*'
	// type ::= '<' ... '>'
	| vectorType
	| labelType
	// type ::= '(' ... ')?'
	| arrayType
	// type ::= structType
	| structType
	// type ::= %FOO
	// type ::= %4
	| namedType
	| mMXType
	| tokenType
;

// --- ( void types )? ----------------------------------------------------------

voidType
	: 'void'
;

// --- ( function types )? ------------------------------------------------------

// REF: parseFunctionType
//
//  ::= type argumentList optionalAttrs


// --- ( integer types )? -------------------------------------------------------

intType
	: INT_TYPE
;

// --- ( floating-POINT types )? ------------------------------------------------

floatType
	: floatKind
;

floatKind
	: 'half'
	| 'float'
	| 'double'
	| 'x86_fp80'
	| 'fp128'
	| 'ppc_fp128'
;

// --- ( mMX types )? -----------------------------------------------------------

mMXType
	: 'x86_mmx'
;

// --- ( pointer types )? -------------------------------------------------------

pointerType
	: type optAddrSpace '*'
;

// REF: parseOptionalAddrSpace
//
//   := 
//   := 'ADDRSPACE' '(' uint32 ')'

optAddrSpace
	: 
	| addrSpace
;

addrSpace
	: 'addrspace' '(' INT_LIT ')'
;

// --- ( vector types )? --------------------------------------------------------

// REF: parseArrayVectorType
//
//     ::= '<' aPSINTVAL 'x' types '>'

vectorType
	: '<' INT_LIT 'x' type '>'
;

// --- ( label types )? ---------------------------------------------------------

labelType
	: 'label'
;

// --- ( token types )? ---------------------------------------------------------

tokenType
	: 'token'
;

// --- ( metadata types )? ------------------------------------------------------

metadataType
	: 'metadata'
;

// --- ( array types )? ---------------------------------------------------------

// REF: parseArrayVectorType
//
//     ::= '(' aPSINTVAL 'x' types ')?'

arrayType
	: '(' INT_LIT 'x' type ')?'
;

// --- ( structure types )? -----------------------------------------------------

// REF: parseStructBody
//
//   structType
//     ::= '(' ')*'
//     ::= '(' type (',' type)* ')*'
//     ::= '<' '(' ')*' '>'
//     ::= '<' '(' type (',' type)* ')*' '>'

// tODO: simplify WHEN PARSER GENERATOR IS NOT LIMITED BY 1 TOKEN LOOKAHEAD.
//
//    structType
//       : '(' types ')*'
//       | '<' '(' types ')*' '>'
//    ;

structType
	: '{' '}'
	| '{' typeList '}'
	| '<' '{' '}' '>'
	| '<' '{' typeList '}' '>'
;

typeList
	: type
	| typeList ',' type
;

opaqueType
	: 'opaque'
;

// --- ( named types )? ---------------------------------------------------------

namedType
	: localIdent
;

// === ( values )? ==============================================================

// REF: parseValue

value
	: constant
	// %42
	// %FOO
	| localIdent
	| inlineAsm
;

// --- ( inline assembler expressions )? ----------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#INLINE-ASSEMBLER-EXPRESSIONS

// REF: parseValID
//
//  ::= 'ASM' sideEffect? alignStack? intelDialect? sTRINGCONSTANT ','
//             sTRINGCONSTANT

inlineAsm
	: 'asm' optSideEffect optAlignStack optIntelDialect stringLit ',' stringLit
;

optSideEffect
	: 
	| 'sideeffect'
;

optAlignStack
	: 
	| 'alignstack'
;

optIntelDialect
	: 
	| 'inteldialect'
;

// === ( constants )? ===========================================================

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#CONSTANTS

// REF: parseValID

constant
	: boolConst
	| intConst
	| floatConst
	| nullConst
	| noneConst
	| structConst
	| arrayConst
	| charArrayConst
	| vectorConst
	| zeroInitializerConst
	// @42
	// @FOO
	| globalIdent
	| undefConst
	| blockAddressConst
	| constantExpr
;

// --- ( boolean constants )? ---------------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#SIMPLE-CONSTANTS

// REF: parseValID

boolConst
	: boolLit
;

boolLit
	: 'true'
	| 'false'
;

// --- ( integer constants )? ---------------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#SIMPLE-CONSTANTS

// REF: parseValID

intConst
	: INT_LIT
;

intLit
	: INT_LIT
;

// --- ( floating-POINT constants )? --------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#SIMPLE-CONSTANTS

// REF: parseValID

floatConst
	: FLOAT_LIT
;

// --- ( null pointer constants )? ----------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#SIMPLE-CONSTANTS

// REF: parseValID

nullConst
	: 'null'
;

// --- ( token constants )? -----------------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#SIMPLE-CONSTANTS

// REF: parseValID

noneConst
	: 'none'
;

// --- ( structure constants )? -------------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#COMPLEX-CONSTANTS

// REF: parseValID
//
//  ::= '(' constVector ')*'
//  ::= '<' '(' constVector ')*' '>' --> packed struct.

// tODO: simplify WHEN PARSER GENERATOR IS NOT LIMITED BY 1 TOKEN LOOKAHEAD.
//
//    structConst
//       : '(' elems ')*'
//       | '<' '(' elems ')*' '>'
//    ;

structConst
	: '{' '}'
	| '{' typeConstList '}'
	| '<' '{' '}' '>'
	| '<' '{' typeConstList '}' '>'
;

// --- ( array constants )? -----------------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#COMPLEX-CONSTANTS

// REF: parseValID
//
//  c 'foo'

arrayConst
	: '(' typeConsts ')?'
;

charArrayConst
	: 'c' stringLit
;

stringLit
	: STRING_LIT
;

// --- ( vector constants )? ----------------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#COMPLEX-CONSTANTS

// REF: parseValID
//
//  ::= '<' constVector '>'         --> vector.

vectorConst
	: '<' typeConsts '>'
;

// --- ( zero initialization constants )? ---------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#COMPLEX-CONSTANTS

// REF: parseValID

zeroInitializerConst
	: 'zeroinitializer'
;

// --- ( undefined values )? ----------------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#UNDEFINED-VALUES

// REF: parseValID

undefConst
	: 'undef'
;

// --- ( addresses OF basic blocks )? -------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#ADDRESSES-OF-BASIC-BLOCKS

// REF: parseValID
//
//  ::= 'BLOCKADDRESS' '(' @FOO ',' %BAR ')'

blockAddressConst
	: 'blockaddress' '(' globalIdent ',' localIdent ')'
;

// === ( constant EXPRESSIONS )? ================================================

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#CONSTANT-EXPRESSIONS

// REF: parseValID

constantExpr
	// binary EXPRESSIONS
	: addExpr
	| fAddExpr
	| subExpr
	| fSubExpr
	| mulExpr
	| fMulExpr
	| uDivExpr
	| sDivExpr
	| fDivExpr
	| uRemExpr
	| sRemExpr
	| fRemExpr
	// bitwise EXPRESSIONS
	| shlExpr
	| lShrExpr
	| aShrExpr
	| andExpr
	| orExpr
	| xorExpr
	// vector EXPRESSIONS
	| extractElementExpr
	| insertElementExpr
	| shuffleVectorExpr
	// aggregate EXPRESSIONS
	| extractValueExpr
	| insertValueExpr
	// memory EXPRESSIONS
	| getElementPtrExpr
	// conversion EXPRESSIONS
	| truncExpr
	| zExtExpr
	| sExtExpr
	| fPTruncExpr
	| fPExtExpr
	| fPToUIExpr
	| fPToSIExpr
	| uIToFPExpr
	| sIToFPExpr
	| ptrToIntExpr
	| intToPtrExpr
	| bitCastExpr
	| addrSpaceCastExpr
	// other EXPRESSIONS
	| iCmpExpr
	| fCmpExpr
	| selectExpr
;

// --- ( binary EXPRESSIONS )? --------------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#CONSTANT-EXPRESSIONS

// ~~~ ( ADD )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

addExpr
	: 'add' overflowFlags '(' type constant ',' type constant ')'
;

// ~~~ ( FADD )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

fAddExpr
	: 'fadd' '(' type constant ',' type constant ')'
;

// ~~~ ( SUB )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

subExpr
	: 'sub' overflowFlags '(' type constant ',' type constant ')'
;

// ~~~ ( FSUB )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

fSubExpr
	: 'fsub' '(' type constant ',' type constant ')'
;

// ~~~ ( MUL )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

mulExpr
	: 'mul' overflowFlags '(' type constant ',' type constant ')'
;

// ~~~ ( FMUL )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

fMulExpr
	: 'fmul' '(' type constant ',' type constant ')'
;

// ~~~ ( UDIV )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

uDivExpr
	: 'udiv' optExact '(' type constant ',' type constant ')'
;

// ~~~ ( SDIV )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

sDivExpr
	: 'sdiv' optExact '(' type constant ',' type constant ')'
;

// ~~~ ( FDIV )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

fDivExpr
	: 'fdiv' '(' type constant ',' type constant ')'
;

// ~~~ ( UREM )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

uRemExpr
	: 'urem' '(' type constant ',' type constant ')'
;

// ~~~ ( SREM )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

sRemExpr
	: 'srem' '(' type constant ',' type constant ')'
;

// ~~~ ( FREM )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

fRemExpr
	: 'frem' '(' type constant ',' type constant ')'
;

// --- ( bitwise EXPRESSIONS )? -------------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#CONSTANT-EXPRESSIONS

// ~~~ ( SHL )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

shlExpr
	: 'shl' overflowFlags '(' type constant ',' type constant ')'
;

// ~~~ ( LSHR )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

lShrExpr
	: 'lshr' optExact '(' type constant ',' type constant ')'
;

// ~~~ ( ASHR )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

aShrExpr
	: 'ashr' optExact '(' type constant ',' type constant ')'
;

// ~~~ ( AND )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

andExpr
	: 'and' '(' type constant ',' type constant ')'
;

// ~~~ ( OR )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

orExpr
	: 'or' '(' type constant ',' type constant ')'
;

// ~~~ ( XOR )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

xorExpr
	: 'xor' '(' type constant ',' type constant ')'
;

// --- ( vector EXPRESSIONS )? --------------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#CONSTANT-EXPRESSIONS

// ~~~ ( EXTRACTELEMENT )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

extractElementExpr
	: 'extractelement' '(' type constant ',' type constant ')'
;

// ~~~ ( INSERTELEMENT )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

insertElementExpr
	: 'insertelement' '(' type constant ',' type constant ',' type constant ')'
;

// ~~~ ( SHUFFLEVECTOR )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

shuffleVectorExpr
	: 'shufflevector' '(' type constant ',' type constant ',' type constant ')'
;

// --- ( aggregate EXPRESSIONS )? -----------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#CONSTANT-EXPRESSIONS

// ~~~ ( EXTRACTVALUE )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

extractValueExpr
	: 'extractvalue' '(' type constant indices ')'
;

// ~~~ ( INSERTVALUE )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

insertValueExpr
	: 'insertvalue' '(' type constant ',' type constant indices ')'
;

// --- ( memory EXPRESSIONS )? --------------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#CONSTANT-EXPRESSIONS

// ~~~ ( GETELEMENTPTR )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

getElementPtrExpr
	: 'getelementptr' optInBounds '(' type ',' type constant ',' gEPConstIndices ')'
;

// REF: parseGlobalValueVector
//
//   ::= 
//   ::= (INRANGE] typeAndValue (',' [INRANGE)? typeAndValue)*

gEPConstIndices
	: 
	| gEPConstIndexList
;

gEPConstIndexList
	: gEPConstIndex
	| gEPConstIndexList ',' gEPConstIndex
;

gEPConstIndex
	: optInrange type constant
;

optInrange
	: 
	| 'inrange'
;

// --- ( conversion EXPRESSIONS )? ----------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#CONSTANT-EXPRESSIONS

// ~~~ ( TRUNC )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

truncExpr
	: 'trunc' '(' type constant 'to' type ')'
;

// ~~~ ( ZEXT )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

zExtExpr
	: 'zext' '(' type constant 'to' type ')'
;

// ~~~ ( SEXT )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

sExtExpr
	: 'sext' '(' type constant 'to' type ')'
;

// ~~~ ( FPTRUNC )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

fPTruncExpr
	: 'fptrunc' '(' type constant 'to' type ')'
;

// ~~~ ( FPEXT )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

fPExtExpr
	: 'fpext' '(' type constant 'to' type ')'
;

// ~~~ ( FPTOUI )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

fPToUIExpr
	: 'fptoui' '(' type constant 'to' type ')'
;

// ~~~ ( FPTOSI )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

fPToSIExpr
	: 'fptosi' '(' type constant 'to' type ')'
;

// ~~~ ( UITOFP )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

uIToFPExpr
	: 'uitofp' '(' type constant 'to' type ')'
;

// ~~~ ( SITOFP )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

sIToFPExpr
	: 'sitofp' '(' type constant 'to' type ')'
;

// ~~~ ( PTRTOINT )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

ptrToIntExpr
	: 'ptrtoint' '(' type constant 'to' type ')'
;

// ~~~ ( INTTOPTR )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

intToPtrExpr
	: 'inttoptr' '(' type constant 'to' type ')'
;

// ~~~ ( BITCAST )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

bitCastExpr
	: 'bitcast' '(' type constant 'to' type ')'
;

// ~~~ ( ADDRSPACECAST )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

addrSpaceCastExpr
	: 'addrspacecast' '(' type constant 'to' type ')'
;

// --- ( other EXPRESSIONS )? ---------------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#CONSTANT-EXPRESSIONS

// ~~~ ( ICMP )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

iCmpExpr
	: 'icmp' iPred '(' type constant ',' type constant ')'
;

// ~~~ ( FCMP )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

fCmpExpr
	: 'fcmp' fPred '(' type constant ',' type constant ')'
;

// ~~~ ( SELECT )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseValID

selectExpr
	: 'select' '(' type constant ',' type constant ',' type constant ')'
;

// === ( basic blocks )? ========================================================

// REF: parseBasicBlock
//
//   ::= labelStr? instruction*

basicBlockList
	: basicBlock+
;

basicBlock
	: optLabelIdent instructions terminator
;

optLabelIdent
	: 
	| labelIdent
;

// === ( instructions )? ========================================================

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#INSTRUCTION-REFERENCE

// REF: parseInstruction

instructions
	: instruction*
;

instruction
	// instructions NOT PRODUCING VALUES.
	: storeInst
	| fenceInst
	| cmpXchgInst
	| atomicRMWInst
	// instructions PRODUCING VALUES.
	| localIdent '=' valueInstruction
	| valueInstruction
;

valueInstruction
	// binary INSTRUCTIONS
	: addInst
	| fAddInst
	| subInst
	| fSubInst
	| mulInst
	| fMulInst
	| uDivInst
	| sDivInst
	| fDivInst
	| uRemInst
	| sRemInst
	| fRemInst
	// bitwise INSTRUCTIONS
	| shlInst
	| lShrInst
	| aShrInst
	| andInst
	| orInst
	| xorInst
	// vector INSTRUCTIONS
	| extractElementInst
	| insertElementInst
	| shuffleVectorInst
	// aggregate INSTRUCTIONS
	| extractValueInst
	| insertValueInst
	// memory INSTRUCTIONS
	| allocaInst
	| loadInst
	| getElementPtrInst
	// conversion INSTRUCTIONS
	| truncInst
	| zExtInst
	| sExtInst
	| fPTruncInst
	| fPExtInst
	| fPToUIInst
	| fPToSIInst
	| uIToFPInst
	| sIToFPInst
	| ptrToIntInst
	| intToPtrInst
	| bitCastInst
	| addrSpaceCastInst
	// other INSTRUCTIONS
	| iCmpInst
	| fCmpInst
	| phiInst
	| selectInst
	| callInst
	| vAArgInst
	| landingPadInst
	| catchPadInst
	| cleanupPadInst
;

// --- ( binary INSTRUCTIONS )? -------------------------------------------------

// ~~~ ( ADD )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#ADD-INSTRUCTION

// REF: parseArithmetic
//
//  ::= arithmeticOps typeAndValue ',' value

addInst
	: 'add' overflowFlags type value ',' value optCommaSepMetadataAttachmentList
;

// ~~~ ( FADD )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#FADD-INSTRUCTION

// REF: parseArithmetic
//
//  ::= arithmeticOps typeAndValue ',' value

fAddInst
	: 'fadd' fastMathFlags type value ',' value optCommaSepMetadataAttachmentList
;

// ~~~ ( SUB )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#SUB-INSTRUCTION

// REF: parseArithmetic
//
//  ::= arithmeticOps typeAndValue ',' value

subInst
	: 'sub' overflowFlags type value ',' value optCommaSepMetadataAttachmentList
;

// ~~~ ( FSUB )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#FSUB-INSTRUCTION

// REF: parseArithmetic
//
//  ::= arithmeticOps typeAndValue ',' value

fSubInst
	: 'fsub' fastMathFlags type value ',' value optCommaSepMetadataAttachmentList
;

// ~~~ ( MUL )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#MUL-INSTRUCTION

// REF: parseArithmetic
//
//  ::= arithmeticOps typeAndValue ',' value

mulInst
	: 'mul' overflowFlags type value ',' value optCommaSepMetadataAttachmentList
;

// ~~~ ( FMUL )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#FMUL-INSTRUCTION

// REF: parseArithmetic
//
//  ::= arithmeticOps typeAndValue ',' value

fMulInst
	: 'fmul' fastMathFlags type value ',' value optCommaSepMetadataAttachmentList
;

// ~~~ ( UDIV )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#UDIV-INSTRUCTION

// REF: parseArithmetic
//
//  ::= arithmeticOps typeAndValue ',' value

uDivInst
	: 'udiv' optExact type value ',' value optCommaSepMetadataAttachmentList
;

// ~~~ ( SDIV )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#SDIV-INSTRUCTION

// REF: parseArithmetic
//
//  ::= arithmeticOps typeAndValue ',' value

sDivInst
	: 'sdiv' optExact type value ',' value optCommaSepMetadataAttachmentList
;

// ~~~ ( FDIV )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#FDIV-INSTRUCTION

// REF: parseArithmetic
//
//  ::= arithmeticOps typeAndValue ',' value

fDivInst
	: 'fdiv' fastMathFlags type value ',' value optCommaSepMetadataAttachmentList
;

// ~~~ ( UREM )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#UREM-INSTRUCTION

// REF: parseArithmetic
//
//  ::= arithmeticOps typeAndValue ',' value

uRemInst
	: 'urem' type value ',' value optCommaSepMetadataAttachmentList
;

// ~~~ ( SREM )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#SREM-INSTRUCTION

// REF: parseArithmetic
//
//  ::= arithmeticOps typeAndValue ',' value

sRemInst
	: 'srem' type value ',' value optCommaSepMetadataAttachmentList
;

// ~~~ ( FREM )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#FREM-INSTRUCTION

// REF: parseArithmetic
//
//  ::= arithmeticOps typeAndValue ',' value

fRemInst
	: 'frem' fastMathFlags type value ',' value optCommaSepMetadataAttachmentList
;

// --- ( bitwise INSTRUCTIONS )? ------------------------------------------------

// ~~~ ( SHL )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#SHL-INSTRUCTION

// REF: parseArithmetic
//
//  ::= arithmeticOps typeAndValue ',' value

shlInst
	: 'shl' overflowFlags type value ',' value optCommaSepMetadataAttachmentList
;

// ~~~ ( LSHR )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#LSHR-INSTRUCTION

// REF: parseArithmetic
//
//  ::= arithmeticOps typeAndValue ',' value

lShrInst
	: 'lshr' optExact type value ',' value optCommaSepMetadataAttachmentList
;

// ~~~ ( ASHR )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#ASHR-INSTRUCTION

// REF: parseArithmetic
//
//  ::= arithmeticOps typeAndValue ',' value

aShrInst
	: 'ashr' optExact type value ',' value optCommaSepMetadataAttachmentList
;

// ~~~ ( AND )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#AND-INSTRUCTION

// REF: parseLogical
//
//  ::= arithmeticOps typeAndValue ',' value {

andInst
	: 'and' type value ',' value optCommaSepMetadataAttachmentList
;

// ~~~ ( OR )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#OR-INSTRUCTION

// REF: parseLogical
//
//  ::= arithmeticOps typeAndValue ',' value {

orInst
	: 'or' type value ',' value optCommaSepMetadataAttachmentList
;

// ~~~ ( XOR )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#XOR-INSTRUCTION

// REF: parseLogical
//
//  ::= arithmeticOps typeAndValue ',' value {

xorInst
	: 'xor' type value ',' value optCommaSepMetadataAttachmentList
;

// --- ( vector INSTRUCTIONS )? -------------------------------------------------

// ~~~ ( EXTRACTELEMENT )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#EXTRACTELEMENT-INSTRUCTION

// REF: parseExtractElement
//
//   ::= 'EXTRACTELEMENT' typeAndValue ',' typeAndValue

extractElementInst
	: 'extractelement' type value ',' type value optCommaSepMetadataAttachmentList
;

// ~~~ ( INSERTELEMENT )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#INSERTELEMENT-INSTRUCTION

// REF: parseInsertElement
//
//   ::= 'INSERTELEMENT' typeAndValue ',' typeAndValue ',' typeAndValue

insertElementInst
	: 'insertelement' type value ',' type value ',' type value optCommaSepMetadataAttachmentList
;

// ~~~ ( SHUFFLEVECTOR )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#SHUFFLEVECTOR-INSTRUCTION

// REF: parseShuffleVector
//
//   ::= 'SHUFFLEVECTOR' typeAndValue ',' typeAndValue ',' typeAndValue

shuffleVectorInst
	: 'shufflevector' type value ',' type value ',' type value optCommaSepMetadataAttachmentList
;

// --- ( aggregate INSTRUCTIONS )? ----------------------------------------------

// ~~~ ( EXTRACTVALUE )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#EXTRACTVALUE-INSTRUCTION

// REF: parseExtractValue
//
//   ::= 'EXTRACTVALUE' typeAndValue (',' uint32)+

extractValueInst
	: 'extractvalue' type value ',' indexList optCommaSepMetadataAttachmentList
;

// ~~~ ( INSERTVALUE )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#INSERTVALUE-INSTRUCTION

// REF: parseInsertValue
//
//   ::= 'INSERTVALUE' typeAndValue ',' typeAndValue (',' uint32)+

insertValueInst
	: 'insertvalue' type value ',' type value ',' indexList optCommaSepMetadataAttachmentList
;

// --- ( memory INSTRUCTIONS )? -------------------------------------------------

// ~~~ ( ALLOCA )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#ALLOCA-INSTRUCTION

// REF: parseAlloc
//
//   ::= 'ALLOCA' 'INALLOCA'? 'SWIFTERROR'? type (',' typeAndValue)?
//       (',' 'ALIGN' i32)? (',', 'ADDRSPACE(n))?

// tODO: simplify WHEN PARSER GENERATOR IS NOT LIMITED BY 1 TOKEN LOOKAHEAD.
//
//    allocaInst
//       : 'alloca' optInAlloca optSwiftError type optCommaTypeValue optCommaAlignment optCommaAddrSpace optCommaSepMetadataAttachmentList
//    ;

allocaInst
	: 'alloca' optInAlloca optSwiftError type optCommaSepMetadataAttachmentList
	| 'alloca' optInAlloca optSwiftError type ',' alignment optCommaSepMetadataAttachmentList
	| 'alloca' optInAlloca optSwiftError type ',' type value optCommaSepMetadataAttachmentList
	| 'alloca' optInAlloca optSwiftError type ',' type value ',' alignment optCommaSepMetadataAttachmentList
	| 'alloca' optInAlloca optSwiftError type ',' addrSpace optCommaSepMetadataAttachmentList
	| 'alloca' optInAlloca optSwiftError type ',' alignment ',' addrSpace optCommaSepMetadataAttachmentList
	| 'alloca' optInAlloca optSwiftError type ',' type value ',' addrSpace optCommaSepMetadataAttachmentList
	| 'alloca' optInAlloca optSwiftError type ',' type value ',' alignment ',' addrSpace optCommaSepMetadataAttachmentList
;

optInAlloca
	: 
	| 'inalloca'
;

optSwiftError
	: 
	| 'swifterror'
;

// ~~~ ( LOAD )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#LOAD-INSTRUCTION

// REF: parseLoad
//
//   ::= 'LOAD' 'VOLATILE'? typeAndValue (',' 'ALIGN' i32)?
//   ::= 'LOAD' 'ATOMIC' 'VOLATILE'? typeAndValue
//       'SINGLETHREAD'? atomicOrdering (',' 'ALIGN' i32)?

// tODO: simplify WHEN PARSER GENERATOR IS NOT LIMITED BY 1 TOKEN LOOKAHEAD.
//
//    loadInst
//       : 'load' optVolatile type ',' type value optCommaAlignment optCommaSepMetadataAttachmentList
//       | 'load' 'atomic' optVolatile type ',' type value optSyncScope atomicOrdering optCommaAlignment optCommaSepMetadataAttachmentList
//    ;

loadInst
	// load.
	: 'load' optVolatile type ',' type value optCommaSepMetadataAttachmentList
	| 'load' optVolatile type ',' type value ',' alignment optCommaSepMetadataAttachmentList
	// atomic LOAD.
	| 'load' 'atomic' optVolatile type ',' type value optSyncScope atomicOrdering optCommaSepMetadataAttachmentList
	| 'load' 'atomic' optVolatile type ',' type value optSyncScope atomicOrdering ',' alignment optCommaSepMetadataAttachmentList
;

// ~~~ ( STORE )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#STORE-INSTRUCTION

// REF: parseStore
//
//   ::= 'STORE' 'VOLATILE'? typeAndValue ',' typeAndValue (',' 'ALIGN' i32)?
//   ::= 'STORE' 'ATOMIC' 'VOLATILE'? typeAndValue ',' typeAndValue
//       'SINGLETHREAD'? atomicOrdering (',' 'ALIGN' i32)?

// tODO: simplify WHEN PARSER GENERATOR IS NOT LIMITED BY 1 TOKEN LOOKAHEAD.
//
//    storeInst
//       : 'store' optVolatile type value ',' type value optCommaAlignment optCommaSepMetadataAttachmentList
//       | 'store' 'atomic' optVolatile type value ',' type value optSyncScope atomicOrdering optCommaAlignment optCommaSepMetadataAttachmentList
//    ;

storeInst
	: 'store' optVolatile type value ',' type value optCommaSepMetadataAttachmentList
	| 'store' optVolatile type value ',' type value ',' alignment optCommaSepMetadataAttachmentList
	| 'store' 'atomic' optVolatile type value ',' type value optSyncScope atomicOrdering optCommaSepMetadataAttachmentList
	| 'store' 'atomic' optVolatile type value ',' type value optSyncScope atomicOrdering ',' alignment optCommaSepMetadataAttachmentList
;

// ~~~ ( FENCE )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#FENCE-INSTRUCTION

// REF: parseFence
//
//   ::= 'FENCE' 'SINGLETHREAD'? atomicOrdering

fenceInst
	: 'fence' optSyncScope atomicOrdering optCommaSepMetadataAttachmentList
;

// ~~~ ( CMPXCHG )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#CMPXCHG-INSTRUCTION

// REF: parseCmpXchg
//
//   ::= 'CMPXCHG' 'WEAK'? 'VOLATILE'? typeAndValue ',' typeAndValue ','
//       typeAndValue 'SINGLETHREAD'? atomicOrdering atomicOrdering

cmpXchgInst
	: 'cmpxchg' optWeak optVolatile type value ',' type value ',' type value optSyncScope atomicOrdering atomicOrdering optCommaSepMetadataAttachmentList
;

optWeak
	: 
	| 'weak'
;

// ~~~ ( ATOMICRMW )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#ATOMICRMW-INSTRUCTION

// REF: parseAtomicRMW
//
//   ::= 'ATOMICRMW' 'VOLATILE'? binOp typeAndValue ',' typeAndValue
//       'SINGLETHREAD'? atomicOrdering

atomicRMWInst
	: 'atomicrmw' optVolatile binOp type value ',' type value optSyncScope atomicOrdering optCommaSepMetadataAttachmentList
;

binOp
	: 'add'
	| 'and'
	| 'max'
	| 'min'
	| 'nand'
	| 'or'
	| 'sub'
	| 'umax'
	| 'umin'
	| 'xchg'
	| 'xor'
;

// ~~~ ( GETELEMENTPTR )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#GETELEMENTPTR-INSTRUCTION

// REF: parseGetElementPtr
//
//   ::= 'GETELEMENTPTR' 'INBOUNDS'? typeAndValue (',' typeAndValue)*

// tODO: simplify WHEN PARSER GENERATOR IS NOT LIMITED BY 1 TOKEN LOOKAHEAD.
//
//    getElementPtrInst
//       : 'getelementptr' optInBounds type ',' type value gEPIndices optCommaSepMetadataAttachmentList
//    ;

getElementPtrInst
	: 'getelementptr' optInBounds type ',' type value optCommaSepMetadataAttachmentList
	| 'getelementptr' optInBounds type ',' type value ',' commaSepTypeValueList optCommaSepMetadataAttachmentList
;

// --- ( conversion INSTRUCTIONS )? ---------------------------------------------

// ~~~ ( TRUNC )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#TRUNC-INSTRUCTION

// REF: parseCast
//
//   ::= castOpc typeAndValue 'TO' type

truncInst
	: 'trunc' type value 'to' type optCommaSepMetadataAttachmentList
;

// ~~~ ( ZEXT )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#ZEXT-INSTRUCTION

// REF: parseCast
//
//   ::= castOpc typeAndValue 'TO' type

zExtInst
	: 'zext' type value 'to' type optCommaSepMetadataAttachmentList
;

// ~~~ ( SEXT )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#SEXT-INSTRUCTION

// REF: parseCast
//
//   ::= castOpc typeAndValue 'TO' type

sExtInst
	: 'sext' type value 'to' type optCommaSepMetadataAttachmentList
;

// ~~~ ( FPTRUNC )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#FPTRUNC-INSTRUCTION

// REF: parseCast
//
//   ::= castOpc typeAndValue 'TO' type

fPTruncInst
	: 'fptrunc' type value 'to' type optCommaSepMetadataAttachmentList
;

// ~~~ ( FPEXT )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#FPEXT-INSTRUCTION

// REF: parseCast
//
//   ::= castOpc typeAndValue 'TO' type

fPExtInst
	: 'fpext' type value 'to' type optCommaSepMetadataAttachmentList
;

// ~~~ ( FPTOUI )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#FPTOUI-INSTRUCTION

// REF: parseCast
//
//   ::= castOpc typeAndValue 'TO' type

fPToUIInst
	: 'fptoui' type value 'to' type optCommaSepMetadataAttachmentList
;

// ~~~ ( FPTOSI )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#FPTOSI-INSTRUCTION

// REF: parseCast
//
//   ::= castOpc typeAndValue 'TO' type

fPToSIInst
	: 'fptosi' type value 'to' type optCommaSepMetadataAttachmentList
;

// ~~~ ( UITOFP )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#UITOFP-INSTRUCTION

// REF: parseCast
//
//   ::= castOpc typeAndValue 'TO' type

uIToFPInst
	: 'uitofp' type value 'to' type optCommaSepMetadataAttachmentList
;

// ~~~ ( SITOFP )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#SITOFP-INSTRUCTION

// REF: parseCast
//
//   ::= castOpc typeAndValue 'TO' type

sIToFPInst
	: 'sitofp' type value 'to' type optCommaSepMetadataAttachmentList
;

// ~~~ ( PTRTOINT )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#PTRTOINT-INSTRUCTION

// REF: parseCast
//
//   ::= castOpc typeAndValue 'TO' type

ptrToIntInst
	: 'ptrtoint' type value 'to' type optCommaSepMetadataAttachmentList
;

// ~~~ ( INTTOPTR )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#INTTOPTR-INSTRUCTION

// REF: parseCast
//
//   ::= castOpc typeAndValue 'TO' type

intToPtrInst
	: 'inttoptr' type value 'to' type optCommaSepMetadataAttachmentList
;

// ~~~ ( BITCAST )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#BITCAST-INSTRUCTION

// REF: parseCast
//
//   ::= castOpc typeAndValue 'TO' type

bitCastInst
	: 'bitcast' type value 'to' type optCommaSepMetadataAttachmentList
;

// ~~~ ( ADDRSPACECAST )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#ADDRSPACECAST-INSTRUCTION

// REF: parseCast
//
//   ::= castOpc typeAndValue 'TO' type

addrSpaceCastInst
	: 'addrspacecast' type value 'to' type optCommaSepMetadataAttachmentList
;

// --- ( other INSTRUCTIONS )? --------------------------------------------------

// ~~~ ( ICMP )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#ICMP-INSTRUCTION

// REF: parseCompare
//
//  ::= 'ICMP' iPredicates typeAndValue ',' value

iCmpInst
	: 'icmp' iPred type value ',' value optCommaSepMetadataAttachmentList
;

// ~~~ ( FCMP )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#FCMP-INSTRUCTION

// REF: parseCompare
//
//  ::= 'FCMP' fPredicates typeAndValue ',' value

fCmpInst
	: 'fcmp' fastMathFlags fPred type value ',' value optCommaSepMetadataAttachmentList
;

// ~~~ ( PHI )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#PHI-INSTRUCTION

// REF: parsePHI
//
//   ::= 'PHI' type '(' value ',' value ']' (',' '[' value ',' value ')?')*

phiInst
	: 'phi' type incList optCommaSepMetadataAttachmentList
;

incList
	: inc (',' inc)*
;

inc
	: '[' value ',' localIdent ']'
;

// ~~~ ( SELECT )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#SELECT-INSTRUCTION

// REF: parseSelect
//
//   ::= 'SELECT' typeAndValue ',' typeAndValue ',' typeAndValue

selectInst
	: 'select' type cond=value ',' type ifTrue=value ',' type ifFalse=value optCommaSepMetadataAttachmentList
;

// ~~~ ( CALL )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#CALL-INSTRUCTION

// REF: parseCall
//
//   ::= 'CALL' optionalFastMathFlags optionalCallingConv
//           optionalAttrs type value parameterList optionalAttrs
//   ::= 'TAIL' 'CALL' optionalFastMathFlags optionalCallingConv
//           optionalAttrs type value parameterList optionalAttrs
//   ::= 'MUSTTAIL' 'CALL' optionalFastMathFlags optionalCallingConv
//           optionalAttrs type value parameterList optionalAttrs
//   ::= 'NOTAIL' 'CALL'  optionalFastMathFlags optionalCallingConv
//           optionalAttrs type value parameterList optionalAttrs

callInst
	: optTail 'call' fastMathFlags optCallingConv returnAttrs type value '(' args ')' funcAttrs operandBundles optCommaSepMetadataAttachmentList
;

optTail
	: 
	| 'musttail'
	| 'notail'
	| 'tail'
;

// ~~~ ( VA_ARG )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#VA_ARG-INSTRUCTION

// REF: parseVA_Arg
//
//   ::= 'VA_ARG' typeAndValue ',' type

vAArgInst
	: 'va_arg' type value ',' type optCommaSepMetadataAttachmentList
;

// ~~~ ( LANDINGPAD )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#LANDINGPAD-INSTRUCTION

// REF: parseLandingPad
//
//   ::= 'LANDINGPAD' type 'PERSONALITY' typeAndValue 'CLEANUP'? clause+
//  clause
//   ::= 'CATCH' typeAndValue
//   ::= 'FILTER'
//   ::= 'FILTER' typeAndValue ( ',' typeAndValue )*

landingPadInst
	: 'landingpad' type optCleanup clauses optCommaSepMetadataAttachmentList
;

optCleanup
	: 
	| 'cleanup'
;

clauses
	: 
	| clauseList
;

clauseList
	: clause
	| clauseList clause
;

clause
	: 'catch' type value
	| 'filter' type arrayConst
;

// --- ( CATCHPAD )? ------------------------------------------------------------

// REF: parseCatchPad
//
//   ::= 'CATCHPAD' paramList 'TO' typeAndValue 'UNWIND' typeAndValue

catchPadInst
	: 'catchpad' 'within' localIdent '(' exceptionArgs ')?' optCommaSepMetadataAttachmentList
;

// --- ( CLEANUPPAD )? ----------------------------------------------------------

// REF: parseCleanupPad
//
//   ::= 'CLEANUPPAD' WITHIN parent paramList

cleanupPadInst
	: 'cleanuppad' 'within' exceptionScope '(' exceptionArgs ')?' optCommaSepMetadataAttachmentList
;

// === ( terminators )? =========================================================

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#TERMINATOR-INSTRUCTIONS

// REF: parseInstruction

terminator
	: retTerm
	| brTerm
	| condBrTerm
	| switchTerm
	| indirectBrTerm
	| invokeTerm
	| resumeTerm
	| catchSwitchTerm
	| catchRetTerm
	| cleanupRetTerm
	| unreachableTerm
;

// --- ( RET )? -----------------------------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#RET-INSTRUCTION

// REF: parseRet
//
//   ::= 'RET' VOID (',' !DBG, !1)*
//   ::= 'RET' typeAndValue (',' !DBG, !1)*

retTerm
	// void RETURN.
	: 'ret' voidType optCommaSepMetadataAttachmentList
	// value RETURN.
	| 'ret' concreteType value optCommaSepMetadataAttachmentList
;

// --- ( BR )? ------------------------------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#BR-INSTRUCTION

// REF: parseBr
//
//   ::= 'BR' typeAndValue
//   ::= 'BR' typeAndValue ',' typeAndValue ',' typeAndValue

// unconditional BRANCH.
brTerm
	: 'br' labelType localIdent optCommaSepMetadataAttachmentList
;

// conditional BRANCH.
condBrTerm
	: 'br' intType value ',' labelType localIdent ',' labelType localIdent optCommaSepMetadataAttachmentList
;

// --- ( SWITCH )? --------------------------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#SWITCH-INSTRUCTION

// REF: parseSwitch
//
//    ::= 'SWITCH' typeAndValue ',' typeAndValue '(' jumpTable ')?'
//  jumpTable
//    ::= (typeAndValue ',' typeAndValue)*

switchTerm
	: 'switch' type value ',' labelType localIdent '(' cases ')?' optCommaSepMetadataAttachmentList
;

cases
	: 
	| caseList
;

caseList
	: caseDecl
	| caseList caseDecl
;

caseDecl
	: type intConst ',' labelType localIdent
;

// --- ( INDIRECTBR )? ----------------------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#INDIRECTBR-INSTRUCTION

// REF: parseIndirectBr
//
//    ::= 'INDIRECTBR' typeAndValue ',' '(' labelList ')?'

indirectBrTerm
	: 'indirectbr' type value ',' '(' labelList ')?' optCommaSepMetadataAttachmentList
;

labelList
	: label
	| labelList ',' label
;

label
	: labelType localIdent
;

// --- ( INVOKE )? --------------------------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#INVOKE-INSTRUCTION

// REF: parseInvoke
//
//   ::= 'INVOKE' optionalCallingConv optionalAttrs type value paramList
//       optionalAttrs 'TO' typeAndValue 'UNWIND' typeAndValue

invokeTerm
	: 'invoke' optCallingConv returnAttrs type value '(' args ')' funcAttrs operandBundles 'to' labelType localIdent 'unwind' labelType localIdent optCommaSepMetadataAttachmentList
;

// --- ( RESUME )? --------------------------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#RESUME-INSTRUCTION

// REF: parseResume
//
//   ::= 'RESUME' typeAndValue

resumeTerm
	: 'resume' type value optCommaSepMetadataAttachmentList
;

// --- ( CATCHSWITCH )? ---------------------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#CATCHSWITCH-INSTRUCTION

// REF: parseCatchSwitch
//
//   ::= 'CATCHSWITCH' WITHIN parent

catchSwitchTerm
	: 'catchswitch' 'within' exceptionScope '(' labelList ')?' 'unwind' unwindTarget optCommaSepMetadataAttachmentList
;

// --- ( CATCHRET )? ------------------------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#CATCHRET-INSTRUCTION

// REF: parseCatchRet
//
//   ::= 'CATCHRET' FROM parent value 'TO' typeAndValue

catchRetTerm
	: 'catchret' 'from' value 'to' labelType localIdent optCommaSepMetadataAttachmentList
;

// --- ( CLEANUPRET )? ----------------------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#CLEANUPRET-INSTRUCTION

// REF: parseCleanupRet
//
//   ::= 'CLEANUPRET' FROM value UNWIND ('TO' 'CALLER' | typeAndValue)

cleanupRetTerm
	: 'cleanupret' 'from' value 'unwind' unwindTarget optCommaSepMetadataAttachmentList
;

// --- ( UNREACHABLE )? ---------------------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#UNREACHABLE-INSTRUCTION

// REF: parseInstruction

unreachableTerm
	: 'unreachable' optCommaSepMetadataAttachmentList
;

// §FRAGMENT___ ( helpers )? §FRAGMENT_____________________________________________________________

unwindTarget
	: 'to' 'caller'
	| labelType localIdent
;
// === ( metadata nodes AND metadata strings )? =================================

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#METADATA-NODES-AND-METADATA-STRINGS

// --- ( metadata tuple )? ------------------------------------------------------

// REF: parseMDTuple

mDTuple
	: '!' mDFields
;

// REF: parseMDNodeVector
//
//   ::= ( element (',' element)* )*
//  element
//   ::= 'NULL' | typeAndValue

// REF: parseMDField(mDFieldList &)

mDFields
	: '{' '}'
	| '{' mDFieldList '}'
;

mDFieldList
	: mDField
	| mDFieldList ',' mDField
;

// REF: parseMDField(mDField &)

mDField
	// null IS a SPECIAL CASE SINCE IT IS TYPELESS.
	: 'null'
	| metadata
;

// --- ( metadata )? ------------------------------------------------------------

// REF: parseMetadata
//
//  ::= i32 %LOCAL
//  ::= i32 @GLOBAL
//  ::= i32 7
//  ::= !42
//  ::= !(...)*
//  ::= !'string'
//  ::= !dILocation(...)

metadata
	: type value
	| mDString
	// !( ... )*
	| mDTuple
	// !7
	| metadataID
	| specializedMDNode
;

// --- ( metadata string )? -----------------------------------------------------

// REF: parseMDString
//
//   ::= '!' sTRINGCONSTANT

mDString
	: '!' stringLit
;

// --- ( metadata attachment )? -------------------------------------------------

// REF: parseMetadataAttachment
//
//   ::= !DBG !42

metadataAttachment
	: metadataName mDNode
;

// --- ( metadata node )? -------------------------------------------------------

// REF: parseMDNode
//
//  ::= !( ... )*
//  ::= !7
//  ::= !dILocation(...)

mDNode
	// !( ... )*
	: mDTuple
	// !42
	| metadataID
	| specializedMDNode
;

// ### ( helper PRODUCTIONS )? ##################################################

// REF: parseOptionalFunctionMetadata
//
//   ::= (!DBG !57)*

metadataAttachments
	: 
	| metadataAttachmentList
;

metadataAttachmentList
	: metadataAttachment
	| metadataAttachmentList metadataAttachment
;

// REF: parseInstructionMetadata
//
//   ::= !DBG !42 (',' !DBG !57)*

optCommaSepMetadataAttachmentList
	: 
	| ',' commaSepMetadataAttachmentList
;

commaSepMetadataAttachmentList
	: metadataAttachment
	| commaSepMetadataAttachmentList ',' metadataAttachment
;

// --- ( specialized metadata nodes )? ------------------------------------------

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#SPECIALIZED-METADATA-NODES

// REF: parseSpecializedMDNode

specializedMDNode
	: dICompileUnit
	| dIFile
	| dIBasicType
	| dISubroutineType
	| dIDerivedType
	| dICompositeType
	| dISubrange
	| dIEnumerator
	| dITemplateTypeParameter
	| dITemplateValueParameter
	| dIModule // NOT IN SPEC AS OF 2018-02-21
	| dINamespace
	| dIGlobalVariable
	| dISubprogram
	| dILexicalBlock
	| dILexicalBlockFile
	| dILocation
	| dILocalVariable
	| dIExpression
	| dIGlobalVariableExpression // NOT IN SPEC AS OF 2018-02-21
	| dIObjCProperty
	| dIImportedEntity
	| dIMacro
	| dIMacroFile
	| genericDINode // NOT IN SPEC AS OF 2018-02-21
;

// ~~~ ( dICompileUnit )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DICOMPILEUNIT

// REF: parseDICompileUnit
//
//   ::= !dICompileUnit(LANGUAGE: dW_LANG_C99, FILE: !0, PRODUCER: 'clang',
//                      ISoptimized: TRUE, FLAGS: '-o2', RUNTIMEversion: 1,
//                      SPLITdebugFilename: 'abc.debug',
//                      EMISSIONkind: fullDebug, ENUMS: !1, RETAINEDtypes: !2,
//                      GLOBALS: !4, IMPORTS: !5, MACROS: !6, DWOid: 0x0abcd)
//
//  rEQUIRED(LANGUAGE, dwarfLangField, );
//  rEQUIRED(FILE, mDField, (allowNull FALSE));
//  oPTIONAL(PRODUCER, mDStringField, );
//  oPTIONAL(ISoptimized, mDBoolField, );
//  oPTIONAL(FLAGS, mDStringField, );
//  oPTIONAL(RUNTIMEversion, mDUnsignedField, (0, uINT32_mAX));
//  oPTIONAL(SPLITdebugFilename, mDStringField, );
//  oPTIONAL(EMISSIONkind, emissionKindField, );
//  oPTIONAL(ENUMS, mDField, );
//  oPTIONAL(RETAINEDtypes, mDField, );
//  oPTIONAL(GLOBALS, mDField, );
//  oPTIONAL(IMPORTS, mDField, );
//  oPTIONAL(MACROS, mDField, );
//  oPTIONAL(DWOid, mDUnsignedField, );
//  oPTIONAL(SPLITdebugInlining, mDBoolField, = TRUE);
//  oPTIONAL(DEBUGinfoForProfiling, mDBoolField, = FALSE);
//  oPTIONAL(GNUpubnames, mDBoolField, = FALSE);

dICompileUnit
	: '!dicompileunit' '(' dICompileUnitFields ')'
;

dICompileUnitFields
	: 
	| dICompileUnitFieldList
;

dICompileUnitFieldList
	: dICompileUnitField
	| dICompileUnitFieldList ',' dICompileUnitField
;

dICompileUnitField
	: 'language:' dwarfLang
	| fileField
	| 'producer:' stringLit
	| isOptimizedField
	| 'flags:' stringLit
	| 'runtime§nonterminal_version:' intLit
	| 'splitdebugfilename:' stringLit
	| 'emission§nonterminal_kind:' emissionKind
	| 'enums:' mDField
	| 'retained§nonterminal_types:' mDField
	| 'globals:' mDField
	| 'imports:' mDField
	| 'macros:' mDField
	| 'dwo§nonterminal_id:' intLit
	| 'splitdebuginlining:' boolLit
	| 'debuginfoforprofiling:' boolLit
	| 'gnu§nonterminal_pubnames:' boolLit
;

// ~~~ ( dIFile )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DIFILE

// REF: parseDIFileType
//
//   ::= !dIFileType(FILENAME: 'path/to/file', DIRECTORY: '/path/to/dir'
//                   CHECKSUMKIND: cSK_MD5,
//                   CHECKSUM: '000102030405060708090a0b0c0d0e0f')
//
//  rEQUIRED(FILENAME, mDStringField, );
//  rEQUIRED(DIRECTORY, mDStringField, );
//  oPTIONAL(CHECKSUMKIND, checksumKindField, (dIFile::cSK_MD5));
//  oPTIONAL(CHECKSUM, mDStringField, );

dIFile
	: '!difile' '(' dIFileFields ')'
;

dIFileFields
	: 
	| dIFileFieldList
;

dIFileFieldList
	: dIFileField
	| dIFileFieldList ',' dIFileField
;

dIFileField
	: 'filename:' stringLit
	| 'directory:' stringLit
	| 'checksumkind:' checksumKind
	| 'checksum:' stringLit
;

// ~~~ ( dIBasicType )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DIBASICTYPE

// REF: parseDIBasicType
//
//   ::= !dIBasicType(TAG: dW_TAG_base_type, NAME: 'int', SIZE: 32, ALIGN: 32)
//
//  oPTIONAL(TAG, dwarfTagField, (DWARF::dW_TAG_base_type));
//  oPTIONAL(NAME, mDStringField, );
//  oPTIONAL(SIZE, mDUnsignedField, (0, uINT64_mAX));
//  oPTIONAL(ALIGN, mDUnsignedField, (0, uINT32_mAX));
//  oPTIONAL(ENCODING, dwarfAttEncodingField, );

dIBasicType
	: '!dibasictype' '(' dIBasicTypeFields ')'
;

dIBasicTypeFields
	: 
	| dIBasicTypeFieldList
;

dIBasicTypeFieldList
	: dIBasicTypeField
	| dIBasicTypeFieldList ',' dIBasicTypeField
;

dIBasicTypeField
	: tagField
	| nameField
	| sizeField
	| alignField
	| 'encoding:' dwarfAttEncoding
;

// ~~~ ( dISubroutineType )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DISUBROUTINETYPE

// REF: parseDISubroutineType
//
//  oPTIONAL(FLAGS, dIFlagField, );
//  oPTIONAL(CC, dwarfCCField, );
//  rEQUIRED(TYPES, mDField, );

dISubroutineType
	: '!disubroutinetype' '(' dISubroutineTypeFields ')'
;

dISubroutineTypeFields
	: 
	| dISubroutineTypeFieldList
;

dISubroutineTypeFieldList
	: dISubroutineTypeField
	| dISubroutineTypeFieldList ',' dISubroutineTypeField
;

dISubroutineTypeField
	: flagsField
	| 'cc:' dwarfCC
	| 'types:' mDField
;

// ~~~ ( dIDerivedType )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DIDERIVEDTYPE

// REF: parseDIDerivedType
//
//   ::= !dIDerivedType(TAG: dW_TAG_pointer_type, NAME: 'int', FILE: !0,
//                      LINE: 7, SCOPE: !1, BASEtype: !2, SIZE: 32,
//                      ALIGN: 32, OFFSET: 0, FLAGS: 0, EXTRAdata: !3,
//                      DWARFaddressSpace: 3)
//
//  rEQUIRED(TAG, dwarfTagField, );
//  oPTIONAL(NAME, mDStringField, );
//  oPTIONAL(SCOPE, mDField, );
//  oPTIONAL(FILE, mDField, );
//  oPTIONAL(LINE, lineField, );
//  rEQUIRED(BASEtype, mDField, );
//  oPTIONAL(SIZE, mDUnsignedField, (0, uINT64_mAX));
//  oPTIONAL(ALIGN, mDUnsignedField, (0, uINT32_mAX));
//  oPTIONAL(OFFSET, mDUnsignedField, (0, uINT64_mAX));
//  oPTIONAL(FLAGS, dIFlagField, );
//  oPTIONAL(EXTRAdata, mDField, );
//  oPTIONAL(DWARFaddressSpace, mDUnsignedField, (uINT32_mAX, uINT32_mAX));

dIDerivedType
	: '!diderivedtype' '(' dIDerivedTypeFields ')'
;

dIDerivedTypeFields
	: 
	| dIDerivedTypeFieldList
;

dIDerivedTypeFieldList
	: dIDerivedTypeField
	| dIDerivedTypeFieldList ',' dIDerivedTypeField
;

dIDerivedTypeField
	: tagField
	| nameField
	| scopeField
	| fileField
	| lineField
	| baseTypeField
	| sizeField
	| alignField
	| offsetField
	| flagsField
	| 'extra§nonterminal_data:' mDField
	| 'dwarfaddressspace:' intLit
;

// ~~~ ( dICompositeType )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DICOMPOSITETYPE

// REF: parseDICompositeType
//
//  rEQUIRED(TAG, dwarfTagField, );
//  oPTIONAL(NAME, mDStringField, );
//  oPTIONAL(SCOPE, mDField, );
//  oPTIONAL(FILE, mDField, );
//  oPTIONAL(LINE, lineField, );
//  oPTIONAL(BASEtype, mDField, );
//  oPTIONAL(SIZE, mDUnsignedField, (0, uINT64_mAX));
//  oPTIONAL(ALIGN, mDUnsignedField, (0, uINT32_mAX));
//  oPTIONAL(OFFSET, mDUnsignedField, (0, uINT64_mAX));
//  oPTIONAL(FLAGS, dIFlagField, );
//  oPTIONAL(ELEMENTS, mDField, );
//  oPTIONAL(RUNTIMElang, dwarfLangField, );
//  oPTIONAL(VTABLEholder, mDField, );
//  oPTIONAL(TEMPLATEparams, mDField, );
//  oPTIONAL(IDENTIFIER, mDStringField, );
//  oPTIONAL(DISCRIMINATOR, mDField, );

dICompositeType
	: '!dicompositetype' '(' dICompositeTypeFields ')'
;

dICompositeTypeFields
	: 
	| dICompositeTypeFieldList
;

dICompositeTypeFieldList
	: dICompositeTypeField
	| dICompositeTypeFieldList ',' dICompositeTypeField
;

dICompositeTypeField
	: tagField
	| nameField
	| scopeField
	| fileField
	| lineField
	| baseTypeField
	| sizeField
	| alignField
	| offsetField
	| flagsField
	| 'elements:' mDField
	| 'runtime§nonterminal_lang:' dwarfLang
	| 'vtable§nonterminal_holder:' mDField
	| templateParamsField
	| 'identifier:' stringLit
	| 'discriminator:' mDField
;

// ~~~ ( dISubrange )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DISUBRANGE

// REF: parseDISubrange
//
//   ::= !dISubrange(COUNT: 30, LOWERbound: 2)
//   ::= !dISubrange(COUNT: !NODE, LOWERbound: 2)
//
//  rEQUIRED(COUNT, mDSignedOrMDField, (-1, -1, iNT64_mAX, FALSE));
//  oPTIONAL(LOWERbound, mDSignedField, );

dISubrange
	: '!disubrange' '(' dISubrangeFields ')'
;

dISubrangeFields
	: 
	| dISubrangeFieldList
;

dISubrangeFieldList
	: dISubrangeField
	| dISubrangeFieldList ',' dISubrangeField
;

dISubrangeField
	: 'count:' intOrMDField
	| 'lower§nonterminal_bound:' intLit
;

// ~~~ ( dIEnumerator )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DIENUMERATOR

// REF: parseDIEnumerator
//
//   ::= !dIEnumerator(VALUE: 30, ISunsigned: TRUE, NAME: 'somekind')
//
//  rEQUIRED(NAME, mDStringField, );
//  rEQUIRED(VALUE, mDSignedOrUnsignedField, );
//  oPTIONAL(ISunsigned, mDBoolField, (FALSE));

dIEnumerator
	: '!dienumerator' '(' dIEnumeratorFields ')'
;

dIEnumeratorFields
	: 
	| dIEnumeratorFieldList
;

dIEnumeratorFieldList
	: dIEnumeratorField
	| dIEnumeratorFieldList ',' dIEnumeratorField
;

dIEnumeratorField
	: nameField
	| 'value:' intLit
	| 'is§nonterminal_unsigned:' boolLit
;

// ~~~ ( dITemplateTypeParameter )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DITEMPLATETYPEPARAMETER

// REF: parseDITemplateTypeParameter
//
//   ::= !dITemplateTypeParameter(NAME: '§nonterminal_ty', TYPE: !1)
//
//  oPTIONAL(NAME, mDStringField, );
//  rEQUIRED(TYPE, mDField, );

dITemplateTypeParameter
	: '!ditemplatetypeparameter' '(' dITemplateTypeParameterFields ')'
;

dITemplateTypeParameterFields
	: 
	| dITemplateTypeParameterFieldList
;

dITemplateTypeParameterFieldList
	: dITemplateTypeParameterField
	| dITemplateTypeParameterFieldList ',' dITemplateTypeParameterField
;

dITemplateTypeParameterField
	: nameField
	| typeField
;

// ~~~ ( dITemplateValueParameter )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DITEMPLATEVALUEPARAMETER

// REF: parseDITemplateValueParameter
//
//   ::= !dITemplateValueParameter(TAG: dW_TAG_template_value_parameter,
//                                 NAME: 'v', TYPE: !1, VALUE: i32 7)
//
//  oPTIONAL(TAG, dwarfTagField, (DWARF::dW_TAG_template_value_parameter));
//  oPTIONAL(NAME, mDStringField, );
//  oPTIONAL(TYPE, mDField, );
//  rEQUIRED(VALUE, mDField, );

dITemplateValueParameter
	: '!ditemplatevalueparameter' '(' dITemplateValueParameterFields ')'
;

dITemplateValueParameterFields
	: 
	| dITemplateValueParameterFieldList
;

dITemplateValueParameterFieldList
	: dITemplateValueParameterField
	| dITemplateValueParameterFieldList ',' dITemplateValueParameterField
;

dITemplateValueParameterField
	: tagField
	| nameField
	| typeField
	| 'value:' mDField
;

// ~~~ ( dIModule )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseDIModule
//
//   ::= !dIModule(SCOPE: !0, NAME: 'somemodule', CONFIGmacros: '-dndebug',
//                 INCLUDEpath: '/usr/include', ISYSROOT: '/')
//
//  rEQUIRED(SCOPE, mDField, );
//  rEQUIRED(NAME, mDStringField, );
//  oPTIONAL(CONFIGmacros, mDStringField, );
//  oPTIONAL(INCLUDEpath, mDStringField, );
//  oPTIONAL(ISYSROOT, mDStringField, );

dIModule
	: '!dimodule' '(' dIModuleFields ')'
;

dIModuleFields
	: 
	| dIModuleFieldList
;

dIModuleFieldList
	: dIModuleField
	| dIModuleFieldList ',' dIModuleField
;

dIModuleField
	: scopeField
	| nameField
	| 'config§nonterminal_macros:' stringLit
	| 'include§nonterminal_path:' stringLit
	| 'isysroot:' stringLit
;

// ~~~ ( dINamespace )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DINAMESPACE

// REF: parseDINamespace
//
//   ::= !dINamespace(SCOPE: !0, FILE: !2, NAME: 'somenamespace', LINE: 9)
//
//  rEQUIRED(SCOPE, mDField, );
//  oPTIONAL(NAME, mDStringField, );
//  oPTIONAL(EXPORTsymbols, mDBoolField, );

dINamespace
	: '!dinamespace' '(' dINamespaceFields ')'
;

dINamespaceFields
	: 
	| dINamespaceFieldList
;

dINamespaceFieldList
	: dINamespaceField
	| dINamespaceFieldList ',' dINamespaceField
;

dINamespaceField
	: scopeField
	| nameField
	| 'export§nonterminal_symbols:' boolLit
;

// ~~~ ( dIGlobalVariable )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DIGLOBALVARIABLE

// REF: parseDIGlobalVariable
//
//   ::= !dIGlobalVariable(SCOPE: !0, NAME: 'foo', LINKAGEname: 'foo',
//                         FILE: !1, LINE: 7, TYPE: !2, ISlocal: FALSE,
//                         ISdefinition: TRUE, DECLARATION: !3, ALIGN: 8)
//
//  rEQUIRED(NAME, mDStringField, (allowEmpty FALSE));
//  oPTIONAL(SCOPE, mDField, );
//  oPTIONAL(LINKAGEname, mDStringField, );
//  oPTIONAL(FILE, mDField, );
//  oPTIONAL(LINE, lineField, );
//  oPTIONAL(TYPE, mDField, );
//  oPTIONAL(ISlocal, mDBoolField, );
//  oPTIONAL(ISdefinition, mDBoolField, (TRUE));
//  oPTIONAL(DECLARATION, mDField, );
//  oPTIONAL(ALIGN, mDUnsignedField, (0, uINT32_mAX));

dIGlobalVariable
	: '!diglobalvariable' '(' dIGlobalVariableFields ')'
;

dIGlobalVariableFields
	: 
	| dIGlobalVariableFieldList
;

dIGlobalVariableFieldList
	: dIGlobalVariableField
	| dIGlobalVariableFieldList ',' dIGlobalVariableField
;

dIGlobalVariableField
	: nameField
	| scopeField
	| linkageNameField
	| fileField
	| lineField
	| typeField
	| isLocalField
	| isDefinitionField
	| declarationField
	| alignField
;

// ~~~ ( dISubprogram )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DISUBPROGRAM

// REF: parseDISubprogram
//
//   ::= !dISubprogram(SCOPE: !0, NAME: 'foo', LINKAGEname: 'nonterminal_zfoo',
//                     FILE: !1, LINE: 7, TYPE: !2, ISlocal: FALSE,
//                     ISdefinition: TRUE, SCOPEline: 8, CONTAININGtype: !3,
//                     VIRTUALITY: dW_VIRTUALTIY_pure_virtual,
//                     VIRTUALindex: 10, THISadjustment: 4, FLAGS: 11,
//                     ISoptimized: FALSE, TEMPLATEparams: !4, DECLARATION: !5,
//                     VARIABLES: !6, THROWNtypes: !7)
//
//  oPTIONAL(NAME, mDStringField, );
//  oPTIONAL(SCOPE, mDField, );
//  oPTIONAL(LINKAGEname, mDStringField, );
//  oPTIONAL(FILE, mDField, );
//  oPTIONAL(LINE, lineField, );
//  oPTIONAL(TYPE, mDField, );
//  oPTIONAL(ISlocal, mDBoolField, );
//  oPTIONAL(ISdefinition, mDBoolField, (TRUE));
//  oPTIONAL(SCOPEline, lineField, );
//  oPTIONAL(CONTAININGtype, mDField, );
//  oPTIONAL(VIRTUALITY, dwarfVirtualityField, );
//  oPTIONAL(VIRTUALindex, mDUnsignedField, (0, uINT32_mAX));
//  oPTIONAL(THISadjustment, mDSignedField, (0, iNT32_mIN, iNT32_mAX));
//  oPTIONAL(FLAGS, dIFlagField, );
//  oPTIONAL(ISoptimized, mDBoolField, );
//  oPTIONAL(UNIT, mDField, );
//  oPTIONAL(TEMPLATEparams, mDField, );
//  oPTIONAL(DECLARATION, mDField, );
//  oPTIONAL(VARIABLES, mDField, );
//  oPTIONAL(THROWNtypes, mDField, );

dISubprogram
	: '!disubprogram' '(' dISubprogramFields ')'
;

dISubprogramFields
	: 
	| dISubprogramFieldList
;

dISubprogramFieldList
	: dISubprogramField
	| dISubprogramFieldList ',' dISubprogramField
;

dISubprogramField
	: nameField
	| scopeField
	| linkageNameField
	| fileField
	| lineField
	| typeField
	| isLocalField
	| isDefinitionField
	| 'scope§nonterminal_line:' intLit
	| 'containing§nonterminal_type:' mDField
	| 'virtuality:' dwarfVirtuality
	| 'virtual§nonterminal_index:' intLit
	| 'this§nonterminal_adjustment:' intLit
	| flagsField
	| isOptimizedField
	| 'unit:' mDField
	| templateParamsField
	| declarationField
	| 'variables:' mDField
	| 'thrown§nonterminal_types:' mDField
;

// ~~~ ( dILexicalBlock )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DILEXICALBLOCK

// REF: parseDILexicalBlock
//
//   ::= !dILexicalBlock(SCOPE: !0, FILE: !2, LINE: 7, COLUMN: 9)
//
//  rEQUIRED(SCOPE, mDField, (allowNull FALSE));
//  oPTIONAL(FILE, mDField, );
//  oPTIONAL(LINE, lineField, );
//  oPTIONAL(COLUMN, columnField, );

dILexicalBlock
	: '!dilexicalblock' '(' dILexicalBlockFields ')'
;

dILexicalBlockFields
	: 
	| dILexicalBlockFieldList
;

dILexicalBlockFieldList
	: dILexicalBlockField
	| dILexicalBlockFieldList ',' dILexicalBlockField
;

dILexicalBlockField
	: scopeField
	| fileField
	| lineField
	| columnField
;

// ~~~ ( dILexicalBlockFile )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DILEXICALBLOCKFILE

// REF: parseDILexicalBlockFile
//
//   ::= !dILexicalBlockFile(SCOPE: !0, FILE: !2, DISCRIMINATOR: 9)
//
//  rEQUIRED(SCOPE, mDField, (allowNull FALSE));
//  oPTIONAL(FILE, mDField, );
//  rEQUIRED(DISCRIMINATOR, mDUnsignedField, (0, uINT32_mAX));

dILexicalBlockFile
	: '!dilexicalblockfile' '(' dILexicalBlockFileFields ')'
;

dILexicalBlockFileFields
	: 
	| dILexicalBlockFileFieldList
;

dILexicalBlockFileFieldList
	: dILexicalBlockFileField
	| dILexicalBlockFileFieldList ',' dILexicalBlockFileField
;

dILexicalBlockFileField
	: scopeField
	| fileField
	| 'discriminator:' intLit
;

// ~~~ ( dILocation )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DILOCATION

// REF: parseDILocation
//
//   ::= !dILocation(LINE: 43, COLUMN: 8, SCOPE: !5, INLINEDat: !6)
//
//  oPTIONAL(LINE, lineField, );
//  oPTIONAL(COLUMN, columnField, );
//  rEQUIRED(SCOPE, mDField, (allowNull FALSE));
//  oPTIONAL(INLINEDat, mDField, );

dILocation
	: '!dilocation' '(' dILocationFields ')'
;

dILocationFields
	: 
	| dILocationFieldList
;

dILocationFieldList
	: dILocationField
	| dILocationFieldList ',' dILocationField
;

dILocationField
	: lineField
	| columnField
	| scopeField
	| 'inlined§nonterminal_at:' mDField
;

// ~~~ ( dILocalVariable )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DILOCALVARIABLE

// REF: parseDILocalVariable
//
//   ::= !dILocalVariable(ARG: 7, SCOPE: !0, NAME: 'foo',
//                        FILE: !1, LINE: 7, TYPE: !2, ARG: 2, FLAGS: 7,
//                        ALIGN: 8)
//   ::= !dILocalVariable(SCOPE: !0, NAME: 'foo',
//                        FILE: !1, LINE: 7, TYPE: !2, ARG: 2, FLAGS: 7,
//                        ALIGN: 8)
//
//  oPTIONAL(NAME, mDStringField, );
//  oPTIONAL(ARG, mDUnsignedField, (0, uINT16_mAX));
//  rEQUIRED(SCOPE, mDField, (allowNull FALSE));
//  oPTIONAL(FILE, mDField, );
//  oPTIONAL(LINE, lineField, );
//  oPTIONAL(TYPE, mDField, );
//  oPTIONAL(FLAGS, dIFlagField, );
//  oPTIONAL(ALIGN, mDUnsignedField, (0, uINT32_mAX));

dILocalVariable
	: '!dilocalvariable' '(' dILocalVariableFields ')'
;

dILocalVariableFields
	: 
	| dILocalVariableFieldList
;

dILocalVariableFieldList
	: dILocalVariableField
	| dILocalVariableFieldList ',' dILocalVariableField
;

dILocalVariableField
	: nameField
	| 'arg:' intLit
	| scopeField
	| fileField
	| lineField
	| typeField
	| flagsField
	| alignField
;

// ~~~ ( dIExpression )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DIEXPRESSION

// REF: parseDIExpression
//
//   ::= !dIExpression(0, 7, -1)

dIExpression
	: '!diexpression' '(' dIExpressionFields ')'
;

dIExpressionFields
	: 
	| dIExpressionFieldList
;

dIExpressionFieldList
	: dIExpressionField
	| dIExpressionFieldList ',' dIExpressionField
;

dIExpressionField
	: INT_LIT
	| dwarfOp
;

// ~~~ ( dIGlobalVariableExpression )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseDIGlobalVariableExpression
//
//   ::= !dIGlobalVariableExpression(VAR: !0, EXPR: !1)
//
//  rEQUIRED(VAR, mDField, );
//  rEQUIRED(EXPR, mDField, );

dIGlobalVariableExpression
	: '!diglobalvariableexpression' '(' dIGlobalVariableExpressionFields ')'
;

dIGlobalVariableExpressionFields
	: 
	| dIGlobalVariableExpressionFieldList
;

dIGlobalVariableExpressionFieldList
	: dIGlobalVariableExpressionField
	| dIGlobalVariableExpressionFieldList ',' dIGlobalVariableExpressionField
;

dIGlobalVariableExpressionField
	: 'var:' mDField
	| 'expr:' mDField
;

// ~~~ ( dIObjCProperty )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DIOBJCPROPERTY

// REF: parseDIObjCProperty
//
//   ::= !dIObjCProperty(NAME: 'foo', FILE: !1, LINE: 7, SETTER: 'set§nonterminal_foo',
//                       GETTER: 'get§nonterminal_foo', ATTRIBUTES: 7, TYPE: !2)
//
//  oPTIONAL(NAME, mDStringField, );
//  oPTIONAL(FILE, mDField, );
//  oPTIONAL(LINE, lineField, );
//  oPTIONAL(SETTER, mDStringField, );
//  oPTIONAL(GETTER, mDStringField, );
//  oPTIONAL(ATTRIBUTES, mDUnsignedField, (0, uINT32_mAX));
//  oPTIONAL(TYPE, mDField, );

dIObjCProperty
	: '!diobjcproperty' '(' dIObjCPropertyFields ')'
;

dIObjCPropertyFields
	: 
	| dIObjCPropertyFieldList
;

dIObjCPropertyFieldList
	: dIObjCPropertyField
	| dIObjCPropertyFieldList ',' dIObjCPropertyField
;

dIObjCPropertyField
	: nameField
	| fileField
	| lineField
	| 'setter:' stringLit
	| 'getter:' stringLit
	| 'attributes:' intLit
	| typeField
;

// ~~~ ( dIImportedEntity )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DIIMPORTEDENTITY

// REF: parseDIImportedEntity
//
//   ::= !dIImportedEntity(TAG: dW_TAG_imported_module, SCOPE: !0, ENTITY: !1,
//                         LINE: 7, NAME: 'foo')
//
//  rEQUIRED(TAG, dwarfTagField, );
//  rEQUIRED(SCOPE, mDField, );
//  oPTIONAL(ENTITY, mDField, );
//  oPTIONAL(FILE, mDField, );
//  oPTIONAL(LINE, lineField, );
//  oPTIONAL(NAME, mDStringField, );

dIImportedEntity
	: '!diimportedentity' '(' dIImportedEntityFields ')'
;

dIImportedEntityFields
	: 
	| dIImportedEntityFieldList
;

dIImportedEntityFieldList
	: dIImportedEntityField
	| dIImportedEntityFieldList ',' dIImportedEntityField
;

dIImportedEntityField
	: tagField
	| scopeField
	| 'entity:' mDField
	| fileField
	| lineField
	| nameField
;

// ~~~ ( dIMacro )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DIMACRO

// REF: parseDIMacro
//
//   ::= !dIMacro(MACINFO: TYPE, LINE: 9, NAME: 'somemacro', VALUE: 'somevalue')
//
//  rEQUIRED(TYPE, dwarfMacinfoTypeField, );
//  oPTIONAL(LINE, lineField, );
//  rEQUIRED(NAME, mDStringField, );
//  oPTIONAL(VALUE, mDStringField, );

dIMacro
	: '!dimacro' '(' dIMacroFields ')'
;

dIMacroFields
	: 
	| dIMacroFieldList
;

dIMacroFieldList
	: dIMacroField
	| dIMacroFieldList ',' dIMacroField
;

dIMacroField
	: typeMacinfoField
	| lineField
	| nameField
	| 'value:' stringLit
;

// ~~~ ( dIMacroFile )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DIMACROFILE

// REF: parseDIMacroFile
//
//   ::= !dIMacroFile(LINE: 9, FILE: !2, NODES: !3)
//
//  oPTIONAL(TYPE, dwarfMacinfoTypeField, (DWARF::dW_MACINFO_start_file));
//  oPTIONAL(LINE, lineField, );
//  rEQUIRED(FILE, mDField, );
//  oPTIONAL(NODES, mDField, );

dIMacroFile
	: '!dimacrofile' '(' dIMacroFileFields ')'
;

dIMacroFileFields
	: 
	| dIMacroFileFieldList
;

dIMacroFileFieldList
	: dIMacroFileField
	| dIMacroFileFieldList ',' dIMacroFileField
;

dIMacroFileField
	: typeMacinfoField
	| lineField
	| fileField
	| 'nodes:' mDField
;

// ~~~ ( genericDINode )? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// REF: parseGenericDINode
//
//   ::= !genericDINode(TAG: 15, HEADER: '...', OPERANDS: (...)*)
//
//  rEQUIRED(TAG, dwarfTagField, );
//  oPTIONAL(HEADER, mDStringField, );
//  oPTIONAL(OPERANDS, mDFieldList, );

genericDINode
	: '!genericdinode' '(' genericDINodeFields ')'
;

genericDINodeFields
	: 
	| genericDINodeFieldList
;

genericDINodeFieldList
	: genericDINodeField
	| genericDINodeFieldList ',' genericDINodeField
;

genericDINodeField
	: tagField
	| 'header:' stringLit
	| 'operands:' mDFields
;

// ### ( helper PRODUCTIONS )? ##################################################

fileField
	: 'file:' mDField
;

isOptimizedField
	: 'is§nonterminal_optimized:' boolLit
;

tagField
	: 'tag:' dwarfTag
;

nameField
	: 'name:' stringLit
;

sizeField
	: 'size:' intLit
;

alignField
	: 'align:' intLit
;

flagsField
	: 'flags:' dIFlagList
;

lineField
	: 'line:' intLit
;

scopeField
	: 'scope:' mDField
;

baseTypeField
	: 'base§nonterminal_type:' mDField
;

offsetField
	: 'offset:' intLit
;

templateParamsField
	: 'template§nonterminal_params:' mDField
;

// REF: parseMDField(mDSignedOrMDField &)

intOrMDField
	: INT_LIT
	| mDField
;

typeField
	: 'type:' mDField
;

linkageNameField
	: 'linkage§nonterminal_name:' stringLit
;

isLocalField
	: 'is§nonterminal_local:' boolLit
;

isDefinitionField
	: 'is§nonterminal_definition:' boolLit
;

declarationField
	: 'declaration:' mDField
;

columnField
	: 'column:' intLit
;

typeMacinfoField
	: 'type:' dwarfMacinfo
;

checksumKind
	// cSK_foo
	: CHECKSUM_KIND
;

// REF: parseMDField(dIFlagField &)
//
//  ::= uint32
//  ::= dIFlagVector
//  ::= dIFlagVector '|' dIFlagFwdDecl '|' uint32 '|' dIFlagPublic

dIFlagList
	: dIFlag
	| dIFlagList '|' dIFlag
;

dIFlag
	: intLit
	// dIFlagFoo
	| DI_FLAG
;

// REF: parseMDField(dwarfAttEncodingField &)

dwarfAttEncoding
	: intLit
	// dW_ATE_foo
	| DWARF_ATT_ENCODING
;

// REF: parseMDField(dwarfCCField &result)

dwarfCC
	: intLit
	// dW_CC_foo
	| DWARF_CC
;

// REF: parseMDField(dwarfLangField &)

dwarfLang
	: intLit
	// dW_LANG_foo
	| DWARF_LANG
;

// REF: parseMDField(dwarfMacinfoTypeField &)

dwarfMacinfo
	: intLit
	// dW_MACINFO_foo
	| DWARF_MACINFO
;

dwarfOp
	// dW_OP_foo
	: DWARF_OP
;

// REF: parseMDField(dwarfTagField &)

dwarfTag
	: intLit
	// dW_TAG_foo
	| DWARF_TAG
;

// REF: parseMDField(dwarfVirtualityField &)

dwarfVirtuality
	: intLit
	// dW_VIRTUALITY_foo
	| DWARF_VIRTUALITY
;

emissionKind
	: intLit
	| 'fulldebug'
	| 'linetablesonly'
	| 'nodebug'
;

// ### ( helper PRODUCTIONS )? ##################################################

typeValues
	: 
	| typeValueList
;

typeValueList
	: typeValue
	| typeValueList typeValue
;

commaSepTypeValueList
	: typeValue
	| commaSepTypeValueList ',' typeValue
;

typeValue
	: type value
;

typeConsts
	: 
	| typeConstList
;

typeConstList
	: typeConst
	| typeConstList ',' typeConst
;

typeConst
	: type constant
;

// REF: parseOptionalAlignment
//
//   ::= 
//   ::= 'ALIGN' 4

alignment
	: 'align' INT_LIT
;

// REF: PARSEallocSizeArguments

allocSize
	: 'allocsize' '(' INT_LIT ')'
	| 'allocsize' '(' INT_LIT ',' INT_LIT ')'
;

// REF: parseParameterList
//
//    ::= '(' ')'
//    ::= '(' arg (',' arg)* ')'
//  arg
//    ::= type optionalAttributes value optionalAttributes

args
	: 
	| '...'
	| argList
	| argList ',' '...'
;

argList
	: arg
	| argList ',' arg
;

// REF: parseMetadataAsValue
//
//  ::= METADATA i32 %LOCAL
//  ::= METADATA i32 @GLOBAL
//  ::= METADATA i32 7
//  ::= METADATA !0
//  ::= METADATA !(...)*
//  ::= METADATA !'string'

arg
	: concreteType paramAttrs value
	| metadataType metadata
;

// REF: parseOrdering
//
//   ::= atomicOrdering

atomicOrdering
	: 'acq_rel'
	| 'acquire'
	| 'monotonic'
	| 'release'
	| 'seq_cst'
	| 'unordered'
;

// REF: parseOptionalCallingConv
//
//   ::= 
//   ::= 'CCC'
//   ::= 'FASTCC'
//   ::= 'INTEL_OCL_BICC'
//   ::= 'COLDCC'
//   ::= 'x86_stdcallcc'
//   ::= 'x86_fastcallcc'
//   ::= 'x86_thiscallcc'
//   ::= 'x86_vectorcallcc'
//   ::= 'ARM_APCSCC'
//   ::= 'ARM_AAPCSCC'
//   ::= 'ARM_AAPCS_VFPCC'
//   ::= 'msp430_intrcc'
//   ::= 'AVR_INTRCC'
//   ::= 'AVR_SIGNALCC'
//   ::= 'PTX_KERNEL'
//   ::= 'PTX_DEVICE'
//   ::= 'SPIR_FUNC'
//   ::= 'SPIR_KERNEL'
//   ::= 'x86_64_sysvcc'
//   ::= 'win64cc'
//   ::= 'WEBKIT_JSCC'
//   ::= 'ANYREGCC'
//   ::= 'PRESERVE_MOSTCC'
//   ::= 'PRESERVE_ALLCC'
//   ::= 'GHCCC'
//   ::= 'SWIFTCC'
//   ::= 'x86_intrcc'
//   ::= 'HHVMCC'
//   ::= 'HHVM_CCC'
//   ::= 'CXX_FAST_TLSCC'
//   ::= 'AMDGPU_VS'
//   ::= 'AMDGPU_LS'
//   ::= 'AMDGPU_HS'
//   ::= 'AMDGPU_ES'
//   ::= 'AMDGPU_GS'
//   ::= 'AMDGPU_PS'
//   ::= 'AMDGPU_CS'
//   ::= 'AMDGPU_KERNEL'
//   ::= 'CC' uINT

optCallingConv
	: 
	| callingConv
;

callingConv
	: 'amdgpu_cs'
	| 'amdgpu_es'
	| 'amdgpu_gs'
	| 'amdgpu_hs'
	| 'amdgpu_kernel'
	| 'amdgpu_ls'
	| 'amdgpu_ps'
	| 'amdgpu_vs'
	| 'anyregcc'
	| 'arm_aapcs_vfpcc'
	| 'arm_aapcscc'
	| 'arm_apcscc'
	| 'avr_intrcc'
	| 'avr_signalcc'
	| 'ccc'
	| 'coldcc'
	| 'cxx_fast_tlscc'
	| 'fastcc'
	| 'ghccc'
	| 'hhvm_ccc'
	| 'hhvmcc'
	| 'intel_ocl_bicc'
	| 'msp430_intrcc'
	| 'preserve_allcc'
	| 'preserve_mostcc'
	| 'ptx_device'
	| 'ptx_kernel'
	| 'spir_func'
	| 'spir_kernel'
	| 'swiftcc'
	| 'webkit_jscc'
	| 'win64cc'
	| 'x86_64_sysvcc'
	| 'x86_fastcallcc'
	| 'x86_intrcc'
	| 'x86_regcallcc'
	| 'x86_stdcallcc'
	| 'x86_thiscallcc'
	| 'x86_vectorcallcc'
	| 'cc' INT_LIT
;

// REF: PARSEoptionalComdat

optComdat
	: 
	| comdat
;

comdat
	: 'comdat'
	| 'comdat' '(' comdatName ')'
;

dereferenceable
	: 'dereferenceable' '(' INT_LIT ')'
	| 'dereferenceable_or_null' '(' INT_LIT ')'
;

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#DLL-STORAGE-CLASSES

// REF: parseOptionalDLLStorageClass
//
//   ::= 
//   ::= 'DLLIMPORT'
//   ::= 'DLLEXPORT'

optDLLStorageClass
	: 
	| dLLStorageClass
;

dLLStorageClass
	: 'dllexport'
	| 'dllimport'
;

optExact
	: 
	| 'exact'
;

// REF: parseExceptionArgs

exceptionArgs
	: 
	| exceptionArgList
;

exceptionArgList
	: exceptionArg
	| exceptionArgList ',' exceptionArg
;

exceptionArg
	: concreteType value
	| metadataType metadata
;

exceptionScope
	: noneConst
	| localIdent
;

// REF: eatFastMathFlagsIfPresent

fastMathFlags
	: 
	| fastMathFlagList
;

fastMathFlagList
	: fastMathFlag
	| fastMathFlagList fastMathFlag
;

fastMathFlag
	: 'afn'
	| 'arcp'
	| 'contract'
	| 'fast'
	| 'ninf'
	| 'nnan'
	| 'nsz'
	| 'reassoc'
;

// REF: parseCmpPredicate

fPred
	: 'false'
	| 'oeq'
	| 'oge'
	| 'ogt'
	| 'ole'
	| 'olt'
	| 'one'
	| 'ord'
	| 'true'
	| 'ueq'
	| 'uge'
	| 'ugt'
	| 'ule'
	| 'ult'
	| 'une'
	| 'uno'
;

// §FRAGMENT___ ( function attribute )? §FRAGMENT__________________________________________________

// REF: parseFnAttributeValuePairs
//
//   ::= <ATTR> | <ATTR> '=' <VALUE>

funcAttrs
	: 
	| funcAttrList
;

funcAttrList
	: funcAttr
	| funcAttrList funcAttr
;

funcAttr
	// NOT USED IN ATTRIBUTE GROUPS.
	: attrGroupID
	// USED IN ATTRIBUTE GROUPS.
	| 'align' '=' INT_LIT
	| 'alignstack' '=' INT_LIT
	// USED IN FUNCTIONS.
	| alignment
	| allocSize
	| stackAlignment
	| stringLit
	| stringLit '=' stringLit
	| 'alwaysinline'
	| 'argmemonly'
	| 'builtin'
	| 'cold'
	| 'convergent'
	| 'inaccessiblemem_or_argmemonly'
	| 'inaccessiblememonly'
	| 'inlinehint'
	| 'jumptable'
	| 'minsize'
	| 'naked'
	| 'nobuiltin'
	| 'noduplicate'
	| 'noimplicitfloat'
	| 'noinline'
	| 'nonlazybind'
	| 'norecurse'
	| 'noredzone'
	| 'noreturn'
	| 'nounwind'
	| 'optnone'
	| 'optsize'
	| 'readnone'
	| 'readonly'
	| 'returns_twice'
	| 'safestack'
	| 'sanitize_address'
	| 'sanitize_hwaddress'
	| 'sanitize_memory'
	| 'sanitize_thread'
	| 'speculatable'
	| 'ssp'
	| 'sspreq'
	| 'sspstrong'
	| 'strictfp'
	| 'uwtable'
	| 'writeonly'
;

optInBounds
	: 
	| 'inbounds'
;

// REF: parseIndexList
//
//    ::=  (',' uint32)+

indices
	: 
	| ',' indexList
;

indexList
	: index
	| indexList ',' index
;

index
	: INT_LIT
;

// REF: parseCmpPredicate

iPred
	: 'eq'
	| 'ne'
	| 'sge'
	| 'sgt'
	| 'sle'
	| 'slt'
	| 'uge'
	| 'ugt'
	| 'ule'
	| 'ult'
;

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#LINKAGE-TYPES

// REF: parseOptionalLinkage
//
//   ::= 
//   ::= 'PRIVATE'
//   ::= 'INTERNAL'
//   ::= 'WEAK'
//   ::= 'WEAK_ODR'
//   ::= 'LINKONCE'
//   ::= 'LINKONCE_ODR'
//   ::= 'AVAILABLE_EXTERNALLY'
//   ::= 'APPENDING'
//   ::= 'COMMON'
//   ::= 'EXTERN_WEAK'
//   ::= 'EXTERNAL'

optLinkage
	: 
	| linkage
;

linkage
	: 'appending'
	| 'available_externally'
	| 'common'
	| 'internal'
	| 'linkonce'
	| 'linkonce_odr'
	| 'private'
	| 'weak'
	| 'weak_odr'
;

optExternLinkage
	: 
	| externLinkage
;

externLinkage
	: 'extern_weak'
	| 'external'
;

// REF: parseOptionalOperandBundles
//
//    ::= 
//    ::= '(' operandBundle [, operandBundle ]* ')?'
//
//  operandBundle
//    ::= BUNDLE-TAG '(' ')'
//    ::= BUNDLE-TAG '(' type value (, type value )?* ')'
//
//  BUNDLE-TAG ::= string constant

operandBundles
	: 
	| '(' operandBundleList ')?'
;

operandBundleList
	: operandBundle
	| operandBundleList operandBundle
;

operandBundle
	: stringLit '(' typeValues ')'
;

overflowFlags
	: 
	| overflowFlagList
;

overflowFlagList
	: overflowFlag
	| overflowFlagList overflowFlag
;

overflowFlag
	: 'nsw'
	| 'nuw'
;

// §FRAGMENT___ ( parameter attribute )? §FRAGMENT_________________________________________________

// REF: parseOptionalParamAttrs

paramAttrs
	: 
	| paramAttrList
;

paramAttrList
	: paramAttr
	| paramAttrList paramAttr
;

// REF: parseOptionalDerefAttrBytes
//
//   ::= 
//   ::= attrKind '(' 4 ')'

paramAttr
	: alignment
	| dereferenceable
	| stringLit
	| 'byval'
	| 'inalloca'
	| 'inreg'
	| 'nest'
	| 'noalias'
	| 'nocapture'
	| 'nonnull'
	| 'readnone'
	| 'readonly'
	| 'returned'
	| 'signext'
	| 'sret'
	| 'swifterror'
	| 'swiftself'
	| 'writeonly'
	| 'zeroext'
;

// REF: parseArgumentList
//
//   ::= '(' argTypeListI ')'
//  argTypeListI
//   ::= 
//   ::= '...'
//   ::= argTypeList ',' '...'
//   ::= argType (',' argType)*

params
	: 
	| '...'
	| paramList
	| paramList ',' '...'
;

paramList
	: param
	| paramList ',' param
;

param
	: type paramAttrs
	| type paramAttrs localIdent
;

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#RUNTIME-PREEMPTION-MODEL

// REF: parseOptionalDSOLocal

optPreemptionSpecifier
	: 
	| preemptionSpecifier
;

preemptionSpecifier
	: 'dso_local'
	| 'dso_preemptable'
;

// §FRAGMENT___ ( return attribute )? §FRAGMENT__________________________________________________

// REF: parseOptionalReturnAttrs

returnAttrs
	: 
	| returnAttrList
;

returnAttrList
	: returnAttr
	| returnAttrList returnAttr
;

returnAttr
	: alignment
	| dereferenceable
	| stringLit
	| 'inreg'
	| 'noalias'
	| 'nonnull'
	| 'signext'
	| 'zeroext'
;

optSection
	: 
	| section
;

section
	: 'section' stringLit
;

// REF: parseOptionalStackAlignment
//
//   ::= 
//   ::= 'ALIGNSTACK' '(' 4 ')'
stackAlignment
	: 'alignstack' '(' INT_LIT ')'
;

// REF: parseScope
//
//   ::= SYNCSCOPE('singlethread' | '<target scope>')?

optSyncScope
	: 
	| 'syncscope' '(' stringLit ')'
;

// REF: parseOptionalThreadLocal
//
//   := 
//   := 'THREAD_LOCAL'
//   := 'THREAD_LOCAL' '(' TLSMODEL ')'

optThreadLocal
	: 
	| threadLocal
;

threadLocal
	: 'thread_local'
	| 'thread_local' '(' tLSModel ')'
;

// REF: parseTLSModel
//
//   := 'LOCALDYNAMIC'
//   := 'INITIALEXEC'
//   := 'LOCALEXEC'

tLSModel
	: 'initialexec'
	| 'localdynamic'
	| 'localexec'
;

// REF: parseOptionalUnnamedAddr

optUnnamedAddr
	: 
	| unnamedAddr
;

unnamedAddr
	: 'local_unnamed_addr'
	| 'unnamed_addr'
;

// HTTPS://LLVM.ORG/DOCS/langRef.HTML#VISIBILITY-STYLES

// REF: parseOptionalVisibility
//
//   ::= 
//   ::= 'DEFAULT'
//   ::= 'HIDDEN'
//   ::= 'PROTECTED'

optVisibility
	: 
	| visibility
;

visibility
	: 'default'
	| 'hidden'
	| 'protected'
;

optVolatile
	: 
	| 'volatile'
;


// ### ( lexical PART )? ########################################################

fragment ASCII_LETTER_UPPER
	: 'A' .. 'Z'
;

fragment ASCII_LETTER_LOWER
	: 'a' .. 'z'
;

fragment ASCII_LETTER
	: ASCII_LETTER_UPPER
	| ASCII_LETTER_LOWER
;

fragment LETTER
	: ASCII_LETTER
	| '$'
	| '-'
	| '.'
	| '_'
;

fragment ESCAPE_LETTER
	: LETTER
	| '\\'
;

fragment DECIMAL_DIGIT
	: '0' .. '9'
;

fragment HEX_DIGIT
	: DECIMAL_DIGIT
	| 'A' .. 'F'
	| 'a' .. 'f'
;

COMMENT : ';' ~[\r\n]* -> skip;

WHITESPACE : ('\u0000' | ' ' | '\t' | '\r' | '\n') -> skip;

// === ( identifiers )? =========================================================

fragment NAME
	: LETTER ( LETTER | DECIMAL_DIGIT )*
;

fragment ESCAPE_NAME
	: ESCAPE_LETTER ( ESCAPE_LETTER | DECIMAL_DIGIT )*
;

fragment QUOTED_NAME
	: QUOTED_STRING
;

fragment ID
	: DECIMALS
;

// --- ( global IDENTIFIERS )? --------------------------------------------------

GLOBAL_IDENT
	: GLOBAL_NAME
	| GLOBAL_ID
;

fragment GLOBAL_NAME
	: '@' ( NAME | QUOTED_NAME )
;

fragment GLOBAL_ID
	: '@' ID
;

// --- ( local IDENTIFIERS )? ---------------------------------------------------

LOCAL_IDENT
	: LOCAL_NAME
	| LOCAL_ID
;

fragment LOCAL_NAME
	: '%' ( NAME | QUOTED_NAME )
;

fragment LOCAL_ID
	: '%' ID
;

// --- ( labels )? --------------------------------------------------------------

//   label             (-a-zA-Z$._0-9)?+:

LABEL_IDENT
	: ( LETTER | DECIMAL_DIGIT ) ( LETTER | DECIMAL_DIGIT )* ':'
	| QUOTED_STRING ':'
;

// --- ( attribute GROUP IDENTIFIERS )? -----------------------------------------

ATTR_GROUP_ID
	: '#' ID
;

// --- ( comdat IDENTIFIERS )? --------------------------------------------------

COMDAT_NAME
	: '$' ( NAME | QUOTED_NAME )
;

// --- ( metadata IDENTIFIERS )? ------------------------------------------------

METADATA_NAME
	: '!' ESCAPE_NAME
;

METADATA_ID
	: '!' ID
;

// dW_TAG_foo
DWARF_TAG
	: 'D' 'W' '_' 'T' 'A' 'G' '_' ( ASCII_LETTER | DECIMAL_DIGIT | '_' )*
;

// dW_ATE_foo
DWARF_ATT_ENCODING
	: 'D' 'W' '_' 'A' 'T' 'E' '_' ( ASCII_LETTER | DECIMAL_DIGIT | '_' )*
;

// dIFlagFoo
DI_FLAG
	: 'D' 'I' 'F' 'l' 'a' 'g' ( ASCII_LETTER | DECIMAL_DIGIT | '_' )*
;

// dW_LANG_foo
DWARF_LANG
	: 'D' 'W' '_' 'L' 'A' 'N' 'G' '_' ( ASCII_LETTER | DECIMAL_DIGIT | '_' )*
;

// dW_CC_foo
DWARF_CC
	: 'D' 'W' '_' 'C' 'C' '_' ( ASCII_LETTER | DECIMAL_DIGIT | '_' )*
;

// cSK_foo
CHECKSUM_KIND
	: 'C' 'S' 'K' '_' ( ASCII_LETTER | DECIMAL_DIGIT | '_' )*
;

// dW_VIRTUALITY_foo
DWARF_VIRTUALITY
	: 'D' 'W' '_' 'V' 'I' 'R' 'T' 'U' 'A' 'L' 'I' 'T' 'Y' '_' ( ASCII_LETTER | DECIMAL_DIGIT | '_' )*
;

// dW_MACINFO_foo
DWARF_MACINFO
	: 'D' 'W' '_' 'M' 'A' 'C' 'I' 'N' 'F' 'O' '_' ( ASCII_LETTER | DECIMAL_DIGIT | '_' )*
;

// dW_OP_foo
DWARF_OP
	: 'D' 'W' '_' 'O' 'P' '_' ( ASCII_LETTER | DECIMAL_DIGIT | '_' )*
;

// === ( integer LITERALS )? ====================================================

//   integer           (-]?[0-9)?+

INT_LIT
	: DECIMAL_LIT
;

fragment DECIMAL_LIT
	: ( '-' )? DECIMALS
;

fragment DECIMALS
	: DECIMAL_DIGIT ( DECIMAL_DIGIT )*
;

// === ( floating-POINT LITERALS )? =============================================

//   fPConstant        (-+]?[0-9]+[.][0-9]*([eE][-+]?[0-9)?+)?

FLOAT_LIT
	: FRAC_LIT
	| SCI_LIT
	| FLOAT_HEX_LIT
;

fragment FRAC_LIT
	: ( SIGN )? DECIMALS '.' ( DECIMAL_DIGIT )*
;

fragment SIGN
	: '+'
	| '-'
;

fragment SCI_LIT
	: FRAC_LIT ( 'e' | 'E' ) ( SIGN )? DECIMALS
;

//   hexFPConstant     0x(0-9A-fa-f)?+     // 16 HEX DIGITS
//   hexFP80constant   0xK(0-9A-fa-f)?+    // 20 HEX DIGITS
//   hexFP128constant  0xL(0-9A-fa-f)?+    // 32 HEX DIGITS
//   hexPPC128constant 0xM(0-9A-fa-f)?+    // 32 HEX DIGITS
//   hexHalfConstant   0xH(0-9A-fa-f)?+    // 4 HEX DIGITS

fragment FLOAT_HEX_LIT
	:  '0' 'x'      HEX_DIGIT ( HEX_DIGIT )*
	|  '0' 'x' 'K'  HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
	|  '0' 'x' 'L'  HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
	|  '0' 'x' 'M'  HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
	|  '0' 'x' 'H'  HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
;

// === ( string LITERALS )? =====================================================

STRING_LIT
	: QUOTED_STRING
;

fragment QUOTED_STRING
	: '"' ( . )*? '"'
;

// === ( types )? ===============================================================

INT_TYPE
	: 'i' DECIMALS
;
