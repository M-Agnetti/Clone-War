package fr.uge.clone;

import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.module.ModuleFinder;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class AsmParser {

    private static String getTypeName(String type){
        var arr = type.split("/");
        return arr[arr.length - 1];
    }

    private static String getOpcode(int opcode){
        var fields = Opcodes.class.getDeclaredFields();
        try{
            for(var field:fields){
                if((int) field.get(field) == opcode){
                    return field.getName();
                }
            }
        } catch(IllegalAccessException e){
            throw new IllegalStateException(e);
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        var list = parse().entrySet().stream().sorted(Map.Entry.comparingByKey())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (e1, e2) -> e1,
                                LinkedHashMap::new
                        ));
        System.out.println(list);
    }

    public static Map<Integer, StringJoiner> parse() throws IOException {
        final ArrayList<String> list = new ArrayList<>();
        final Map<Integer, StringJoiner> map = new HashMap<>();

        var finder = ModuleFinder.of(Path.of("test2.jar"));
        var moduleReference = finder.findAll().stream().findFirst().orElseThrow();
        try(var reader = moduleReference.open()) {
            for(var filename: (Iterable<String>) reader.list()::iterator) {
                if (!filename.endsWith(".class")) {
                    continue;
                }
                try(var inputStream = reader.open(filename).orElseThrow()) {
                    var classReader = new ClassReader(inputStream);
                    classReader.accept(new ClassVisitor(Opcodes.ASM9) {

                        private static String modifier(int access) {
                            if (Modifier.isPublic(access)) {
                                return "public";
                            }
                            if (Modifier.isPrivate(access)) {
                                return "private";
                            }
                            if (Modifier.isProtected(access)) {
                                return "protected";
                            }
                            return "";
                        }

                        @Override
                        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                            System.err.println("class " + modifier(access) + " " + name + " " + superName + " " + (interfaces != null? Arrays.toString(interfaces): ""));
                        }

                        @Override
                        public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
                            System.err.println("  component " + name + " " + ClassDesc.ofDescriptor(descriptor).displayName());
                            return null;
                        }

                        @Override
                        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                            System.err.println("  field " + modifier(access) + " " + ClassDesc.ofDescriptor(descriptor).displayName());
                            return null;
                        }

                        @Override
                        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                            System.err.println("\n method " + modifier(access) + " " + name + " " + MethodTypeDesc.ofDescriptor(descriptor).displayDescriptor() + " " + signature);
                            return new MethodVisitor(Opcodes.ASM9) {
                                private int lineNumber = -1;
                                private StringJoiner bytecode = new StringJoiner("\n");

                                @Override
                                public void visitInsn(int opcode) {
                                    map.computeIfAbsent(lineNumber, k -> new StringJoiner("\n")).add(getOpcode(opcode));
                                    System.err.println("    visitInsn : " + getOpcode(opcode) + " | line " + lineNumber);
                                }

                                @Override
                                public void visitIntInsn(int opcode, int operand){
                                    System.err.println("    visitIntInsn : " + getOpcode(opcode) + " operand : " + operand + " | line " + lineNumber);
                                }

                                @Override
                                public void visitVarInsn(int opcode, int var){
                                    var s = getOpcode(opcode);
                                    var opName = s.endsWith("LOAD") ? "LOAD" : s.startsWith("V") ? "STORE" : s;
                                    map.computeIfAbsent(lineNumber, k -> new StringJoiner("\n")).add(opName + " " + var);
                                    System.err.println("    visitVarInsn : " + getOpcode(opcode) + " " + var + " | line " + lineNumber);
                                }

                                @Override
                                public void visitTypeInsn(int opcode, String desc){
                                    map.computeIfAbsent(lineNumber, k -> new StringJoiner("\n")).add(getOpcode(opcode));
                                    System.err.println("    visitTypeInsn : " + getOpcode(opcode)  + " | line " + lineNumber);
                                }

                                @Override
                                public void visitFieldInsn(int opc, String owner, String name, String desc){
                                    System.err.println("    visitFieldInsn : " + getOpcode(opc) + " " + name);
                                }

                                @Override
                                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                                    var s = getOpcode(opcode);
                                    var opName = s.endsWith("STATIC") ? (s + name) : s;
                                    map.computeIfAbsent(lineNumber, k -> new StringJoiner("\n")).add(opName);
                                    System.err.println("    " + getOpcode(opcode) + " " + name + " | line " + lineNumber);
                                }

                                // + the other visit methods to get all the opcodes

                                @Override
                                public void visitIincInsn(int var, int increment){
                                    map.computeIfAbsent(lineNumber, k -> new StringJoiner("\n")).add("INCREMENT " + increment);
                                    System.err.println("visitIincInsn : " + var + " " + increment + " | line " + lineNumber);
                                }

                                @Override
                                public void visitJumpInsn(int opcode, Label label){
                                    map.computeIfAbsent(lineNumber, k -> new StringJoiner("\n")).add(getOpcode(opcode) + " " + label);
                                    System.err.println("    visitJumpInsn : " + getOpcode(opcode) + " " + label.toString()
                                            + " | line " + lineNumber);
                                }

                                @Override
                                public void visitLdcInsn(Object cst){
                                    map.computeIfAbsent(lineNumber, k -> new StringJoiner("\n")).add("LDC " + cst.toString());
                                    System.err.println("    visitLdcInsn : LDC " + cst.toString() + " | line " + lineNumber);
                                }

                                @Override
                                public void visitLineNumber(int line, Label start){
                                    this.lineNumber = line;
                                    bytecode = new StringJoiner("\n");
                                }
                            };
                        }
                    }, 0);
                }
            }
        }
        return map;
    }
}
