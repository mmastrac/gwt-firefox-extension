#!/usr/bin/env python

"""Print a GWT binding file for the IDL files specified on the command line"""

import sys, os.path, re, xpidl

# Various modes for parameters
JAVA_METHOD, JSNI_CALL, CALLBACK_FUNCTION, CALLBACK_JSNI_REFERENCE, CALLBACK_CALL = range(5);

printdoccomments = True

if printdoccomments:
    def printComments(fd, clist, indent):
        for c in clist:
            fd.write("%s%s\n" % (indent, c))
else:
    def printComments(fd, clist, indent):
        pass

def getTypeName(rawtype, name, direction):
    if isinstance(rawtype, xpidl.Native):
        type = rawtype.name
        
        if type == 'nsQIResult':
            return 'nsISupports'
        elif type == 'nsIID' or type == 'nsIIDRef' or type == 'nsIIDPtr':
            return 'XPConnectIID<T_' + name + '>'
                
        type = rawtype.nativename.strip()
    
        if type in ['String', 'string', 'wstring', 'AString', 'nsACString', 'ACString', 'AUTF8String', 'DOMString', 'nsAString']:
            type = "String"
        else:
            type = "com.google.gwt.core.client.JavaScriptObject"

        return type
        
    if isinstance(rawtype, xpidl.Builtin):
        type = rawtype.name.strip()

        if type in ['String', 'string', 'wstring', 'AString', 'ACString', 'AUTF8String', 'DOMString', 'nsAString']:
            type = "String"
        elif type == 'int' or type == 'PRInt32' or type == 'PRUint32' or type == 'octet' or type == 'PRUint16' or type == 'long' or type == 'unsigned short' or type == 'short' or type == 'unsigned long' or type == 'wchar':
            type = mapSimpleType("int", "Integer", direction)
        elif type == 'float' or type == 'double' or type == 'long long' or type == 'unsigned long long':
            type = mapSimpleType("double", "Double", direction)
        elif type == 'boolean' or type == 'PRBool':
            type = mapSimpleType("boolean", "Boolean", direction)
        
        return type
        
    if isinstance(rawtype, xpidl.Forward):
        return rawtype.name

    return rawtype.name

def unwrapType(rawtype):
    if isinstance(rawtype, xpidl.Typedef):
        return unwrapType(rawtype.realtype)
    if isinstance(rawtype, xpidl.Array):
        rawtype, postfix = unwrapType(rawtype.type)
        return [rawtype, postfix + '[]']
    
    return [rawtype, '']
    
def mapSimpleType(valueType, refType, direction):
    if direction == 'out' or direction == 'inout':
        return refType
      
    return valueType

def mapTypeJSNI(hastype, direction):
    type = mapType(hastype, direction)
    prefix = ""
    
    # Strip off generic classnames
    if type.find("<") != -1:
        type = type[:type.find("<")]    
    
    while type.find("[]") != -1:
        type = type[:type.find("[]")]
        prefix += "["    
    
    if type == "String":
        return prefix + "Ljava/lang/String;"
        
    if type == "int":
        return prefix + "I"

    if type == "boolean":
        return prefix + "Z"

    if type == "double":
        return prefix + "D"

    if type == "com.google.gwt.core.client.JavaScriptObject":
        return prefix + "Lcom/google/gwt/core/client/JavaScriptObject;"

    if type.find("XP") == 0:
        return prefix + prefix + "Lorg/mozilla/xpconnect/" + type + ";"

    return prefix + "Lorg/mozilla/xpconnect/gecko/" + type + ";"

def mapType(hastype, direction):
    if isinstance(hastype, xpidl.Param) and hastype.iid_is:
        return "T_" + hastype.iid_is

    rawtype, postfix = unwrapType(hastype.realtype)
    
    type = getTypeName(rawtype, hastype.name, direction) + postfix

    if direction == 'out':
        return "XPOut<" + type + ">"
      
    if direction == 'inout':
        return "XPInOut<" + type + ">"
      
    return type

def firstCap(str):
    return str[0].upper() + str[1:]

def firstLower(str):
    return str[0].lower() + str[1:]

def attributeParamName(a):
    return "value"

def attributeNativeName(a, getter):
    binaryname = a.binaryname is not None and a.binaryname or firstCap(a.name)
    return "%s%s" % (getter and 'get' or 'set', binaryname)

def attributeParamlist(a, getter):
    if getter:
        return ''
    return "%s %s" % (mapType(a, 'in'),
                     attributeParamName(a))

def attributeReturnType(a, getter):
    type = mapType(a, 'retval')
    template = ''
    if type.find('XPConnectIID') != -1:
        template = '<T_' + a.name + ' extends nsISupports> '
        
    if getter:
        return template + type 
      
    return template + 'void'

def attributeAsNative(a, getter):
        params = {'retval': attributeReturnType(a, getter),
                  'binaryname': attributeNativeName(a, getter),
                  'paramlist': attributeParamlist(a, getter)}
        return "public final native %(retval)s %(binaryname)s(%(paramlist)s)" % params

def methodNativeName(m, type):
    name = m.binaryname is not None and m.binaryname or m.name
    if type in [JAVA_METHOD, CALLBACK_CALL]:
        name = firstLower(name)
    if name in ['break', 'import', 'assert']:
        return name + '_'
      
    return name

def methodReturnType(m, macro):
    templates = []

    for p in m.params:
        type = mapType(p, p.paramtype)
        if type.find('XPConnectIID') != -1:
            templates.append("T_" + p.name + ' extends nsISupports')
    
    retVal = mapType(m, 'retval')
    if retVal.find('XPConnectIID') != -1:
        templates.append("T_" + m.name + ' extends nsISupports')
        
    template = ''
    if len(templates) > 0:
        template = '<' + ', '.join(templates) + '> '
    
    for p in m.params:
        if p.retval:
            return template + mapType(p, 'retval')
    
    return template + retVal


def methodAs(iface, m, type):
    if type == JAVA_METHOD:
        return "%s %s(%s)" % (methodReturnType(m, 'NS_IMETHOD'),
                            methodNativeName(m, type=type),
                            paramlistAs(m, type=type))
    elif type == CALLBACK_CALL:
        return "@org.mozilla.xpconnect.gecko.%s$Callback::%s(%s)(%s)" % (iface.name,
                              methodNativeName(m, type=type),
                              paramlistAs(m, type=CALLBACK_JSNI_REFERENCE),
                              paramlistAs(m, type=type))
    elif type == CALLBACK_FUNCTION:
    	# Callback functions are anonymous
        return "(%s)" % paramlistAs(m, type=type)
    elif type == JSNI_CALL:
        return "%s(%s)" % (jsSetter(methodNativeName(m, type=type)),
                          paramlistAs(m, type=type))
    else:
        return "%s(%s)" % (methodNativeName(m, type=type),
                          paramlistAs(m, type=type))

def paramlistAs(m, type, empty=''):
    l = list(m.params)
    rettype = m.realtype
    notxpcom = m.notxpcom

    if len(l) == 0:
        return empty

    if type == CALLBACK_JSNI_REFERENCE:
        return "".join([paramAs(p, type=type) for p in l if not p.retval])
    
    return ", ".join([paramAs(p, type=type) for p in l if not p.retval])

def paramAs(p, type):
    if type == CALLBACK_JSNI_REFERENCE:
        return mapTypeJSNI(p, p.paramtype)
        
    if type == JSNI_CALL and isinstance(p.realtype, xpidl.Array):
        return "@com.google.gwt.core.client.GWT::isScript()() ? %s : @org.mozilla.xpconnect.XPConnect::javaArrayToJavaScriptArray(Ljava/lang/Object;)(%s)" % (p.name, p.name);
    
    if type == CALLBACK_CALL and mapType(p, p.paramtype) == "int":
        # Ensure that value comes out as an int
        return "(%s | 0)" % (p.name)

    if type == JAVA_METHOD:
        return "%s %s" % (mapType(p, p.paramtype),
                           p.name)

    return "%s" % (p.name)

def paramlistNames(l, rettype, notxpcom):
    names = [p.name for p in l]
    if not notxpcom and rettype.name != 'void':
        names.append('_retval')
    if len(names) == 0:
        return ''
    return ', '.join(names)

def jsSetter(name):
	if name in ['break', 'debugger', 'delete', 'import', 'abstract']:
		return "['" + name + "']"
		
	return "." + name 

header = """/*
 * DO NOT EDIT.  THIS FILE IS GENERATED FROM %(filename)s
 */

package org.mozilla.xpconnect.gecko;

import org.mozilla.xpconnect.*;
"""

include = ""

header_end = ""

footer = """
/* End of class */
"""

forward_decl = ""

def idl_basename(f):
    """returns the base name of a file with the last extension stripped"""
    return os.path.basename(f).rpartition('.')[0]

def print_header(idl, outputdir, filename):
    for p in idl.productions:
        if p.kind == 'interface':        
            fd = open(outputdir + '/' + p.name + '.java', 'w');
            fd.write(header % {'filename': filename,
                               'basename': idl_basename(filename)})
        
            fd.write('\n')
            fd.write(header_end)
            write_interface(p, fd)
            fd.write(footer % {'basename': idl_basename(filename)})
            continue


iface_header = " {"

uuid_decoder = re.compile(r"""(?P<m0>[a-f0-9]{8})-
                              (?P<m1>[a-f0-9]{4})-
                              (?P<m2>[a-f0-9]{4})-
                              (?P<m3>[a-f0-9]{4})-
                              (?P<m4>[a-f0-9]{12})$""", re.X)

iface_prolog = r"""
  /**
    * IID for interface %(name)s.
    */
  public static final String %(defname)s_IID = "%(iid)s";

  /**
   * Protected JSNI constructor.
   */
  protected %(name)s() {
  }
  
  public static native %(name)s createInstance(String className) /*-{
    return Components.classes[className].createInstance(Components.interfaces.%(name)s);
  }-*/;
  
  public static native %(name)s getService(String className) /*-{
    return Components.classes[className].getService(Components.interfaces.%(name)s);
  }-*/;
  
  public static native XPConnectIID<? extends %(name)s> iid() /*-{
    return Components.interfaces.%(name)s;
  }-*/;

"""

iface_epilog = ""

iface_forward = ""

iface_forward_safe = ""

iface_template_prolog = ""

example_tmpl = ""

iface_template_epilog = ""

def write_interface(iface, fd):
    if iface.namemap is None:
        raise Exception("Interface was not resolved.")

    def write_const_decl(c):
        printComments(fd, c.doccomments, '  ')

        basetype = c.basetype
        value = c.getValue()

        if value > 0x7fffffffL:
          fd.write("  public static final int %(name)s = constant_%(name)s();\n\n  public final native static int constant_%(name)s() /*-{ \n    return %(value)s;\n  }-*/;\n\n" % {
                     'name': c.name,
                     'value': value})
        else:
          fd.write("  public static final int %(name)s = %(value)s;\n" % {
                     'name': c.name,
                     'value': value})

    def write_method_decl(iface, m):
        printComments(fd, m.doccomments, '  ')

        fd.write("  /* %s */\n" % m.toIDL())
        if m.isScriptable():
            if m.name == 'toString':
                return
              
            fd.write("  public final native %s /*-{\n    return this%s;\n  }-*/;\n\n" % (methodAs(iface, m, JAVA_METHOD), methodAs(iface, m, JSNI_CALL)))

    def write_callback_method_decl(iface, m):
        printComments(fd, m.doccomments, '    ')

        fd.write("    /* %s */\n" % m.toIDL())
        if m.isScriptable():
            if m.name == 'toString':
                return
              
            fd.write("    public %s;\n\n" % methodAs(iface, m, JAVA_METHOD))                                                                           
    
    def write_callback_wrapped_method_decl(iface, m):
        fd.write("    wrapped%s = function %s { return callback.%s; }\n" % (jsSetter(m.name), methodAs(iface, m, CALLBACK_FUNCTION), methodAs(iface, m, CALLBACK_CALL)))
             
    def write_attr_decl(a):
        printComments(fd, a.doccomments, '  ')

        fd.write("  /* %s */\n" % a.toIDL());

        fd.write("  %s /*-{\n    return this%s;\n  }-*/;\n\n" % (attributeAsNative(a, True), jsSetter(a.name)))
        if not a.readonly:
            fd.write("  %s /*-{\n    this%s = value;\n  }-*/;\n\n" % (attributeAsNative(a, False), jsSetter(a.name)))
        fd.write("\n")

    # A method is callback-compatible if all of its methods are scriptable and it contains no attributes
    def is_callback_compatible(iface):
        # This is compatible
        if iface.name == "nsISupports":
            return True
    
        for member in iface.members:
            if isinstance(member, xpidl.Attribute):
                return False
            elif isinstance(member, xpidl.Method):
                if not member.isScriptable():
                    return False
                    
        return True;                

    defname = iface.name.upper()
    if iface.name[0:2] == 'ns':
        defname = 'NS_' + defname[2:]

    names = uuid_decoder.match(iface.attributes.uuid).groupdict()
    m3str = names['m3'] + names['m4']
    names['m3joined'] = ", ".join(["0x%s" % m3str[i:i+2] for i in xrange(0, 16, 2)])

    if iface.name[2] == 'I':
        implclass = iface.name[:2] + iface.name[3:]
    else:
        implclass = '_MYCLASS_'

    names.update({'defname': defname,
                  'macroname': iface.name.upper(),
                  'name': iface.name,
                  'iid': iface.attributes.uuid,
                  'implclass': implclass})

    printComments(fd, iface.doccomments, '')

    fd.write("public class ")
    foundcdata = False
    for m in iface.members:
        if isinstance(m, xpidl.CDATA):
            foundcdata = True

    if not foundcdata:
        fd.write("/* NS_NO_VTABLE */ ")

    if iface.attributes.scriptable:
        fd.write("/* NS_SCRIPTABLE */ ")
    if iface.attributes.deprecated:
        fd.write("/* NS_DEPRECATED */ ")
    fd.write(iface.name)
    if iface.base:
        fd.write(" extends %s" % iface.base)
    else:
        fd.write(" extends XPConnectObject")
    fd.write(iface_header % names)
    fd.write(iface_prolog % names)

    if is_callback_compatible(iface):
        fd.write("  /**\n")
        fd.write("   * Callback interface for {@link %s}.\n" % iface.name)
        fd.write("   */\n")
    else:
        fd.write("  /**\n")
        fd.write("   * {@link %s} is not compatible with callback wrapping.  This interface is only available to satisfy the compiler.\n" % iface.name)
        fd.write("   */\n")
        fd.write("  @Deprecated\n")

    fd.write("  public interface Callback extends ")
    if iface.base:
        fd.write(iface.base)
    else:
        fd.write("XPConnectObject")
    fd.write(".Callback {\n")    
    
    if is_callback_compatible(iface) and iface.name != "nsISupports":
        for member in iface.members:
            if isinstance(member, xpidl.Method):
                if member.isScriptable():
                    write_callback_method_decl(iface, member)    

    fd.write("  }\n\n")

    if is_callback_compatible(iface):
        fd.write("  public static native %s wrap(Callback callback) /*-{\n" % iface.name);
        if iface.base:
            fd.write("    var wrapped = @org.mozilla.xpconnect.gecko.%s::wrap(Lorg/mozilla/xpconnect/gecko/%s$Callback;)(callback);\n" % (iface.base, iface.base));
        else:
            fd.write("    var wrapped = {};\n");
        if iface.name != "nsISupports":
            for member in iface.members:
                if isinstance(member, xpidl.Method):
                    write_callback_wrapped_method_decl(iface, member)    
        fd.write("    return wrapped;\n");
        fd.write("  }-*/;\n\n");
    else:
        fd.write("  public static %s wrap(Callback callback) {\n" % iface.name);
        fd.write("    throw new RuntimeException(\"Cannot wrap this object\");\n");
        fd.write("  }\n\n");
        
    for member in iface.members:
        if isinstance(member, xpidl.ConstMember):
            write_const_decl(member)
        elif isinstance(member, xpidl.Method):
            write_method_decl(iface, member)
        elif isinstance(member, xpidl.CDATA):
            pass
        elif isinstance(member, xpidl.Attribute):
            write_attr_decl(member)
        else:
            raise Exception("Unexpected interface member: %s" % member)

    def emitTemplate(tmpl):
        for member in iface.members:
            if isinstance(member, xpidl.Attribute):
                fd.write(tmpl % {'asNative': attributeAsNative(member, True),
                                 'nativeName': attributeNativeName(member, True),
                                 'paramList': attributeParamName(member)})
                if not member.readonly:
                    fd.write(tmpl % {'asNative': attributeAsNative(member, False),
                                     'nativeName': attributeNativeName(member, False),
                                     'paramList': attributeParamName(member)})
            elif isinstance(member, xpidl.Method):
                fd.write(tmpl % {'asNative': methodAs(iface, member, JAVA_METHOD),
                                 'nativeName': methodNativeName(member),
                                 'paramList': paramlistNames(member.params, member.realtype, member.notxpcom)})
        if len(iface.members) == 0:
            fd.write('\\\n  /* no methods! */')
        elif not member.kind in ('attribute', 'method'):
            fd.write('\\')

    fd.write("}")

if __name__ == '__main__':
    from optparse import OptionParser
    o = OptionParser()
    o.add_option('-I', action='append', dest='incdirs', help="Directory to search for imported files", default=[])
    o.add_option('-O', dest='outputdir', help="Output directory")
    o.add_option('--cachedir', dest='cachedir', help="Directory in which to cache lex/parse tables.", default='')
    options, args = o.parse_args()
    files = args

    if options.cachedir != '':
        sys.path.append(options.cachedir)

    for file in files:
        p = xpidl.IDLParser(outputdir=options.cachedir)
        idl = p.parse(open(file).read(), filename=file)
        idl.resolve(options.incdirs, p)
        print_header(idl, options.outputdir, file)
