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
        var map = parse("src/main/resources/jarFiles/10206324738319095331/classes.jar").entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey,
                            entry -> entry.getValue().entrySet().stream().sorted(Map.Entry.comparingByKey())
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                            (e1, e2) -> e1,
                                            LinkedHashMap::new
                                    )
                        )));
        map.forEach((key, value) -> {
            System.out.println(key + ": ");
            System.out.println(value);
        });
    }

    public static Map<String, Map<Integer, StringJoiner>> parse(String path) throws IOException {
        final Map<String, Map<Integer, StringJoiner>> map = new HashMap<>();

        var finder = ModuleFinder.of(Path.of(path));
        var moduleReference = finder.findAll().stream().findFirst().orElseThrow();
        try(var reader = moduleReference.open()) {
            for(var filename: (Iterable<String>) reader.list()::iterator) {
                if (!filename.endsWith(".class")) {
                    continue;
                }
                try(var inputStream = reader.open(filename).orElseThrow()) {
                    var classReader = new ClassReader(inputStream);
                    classReader.accept(new ClassVisitor(Opcodes.ASM9) {
                        String fileName;

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
                            fileName = name;
                            //System.err.println("class " + modifier(access) + " " + name + " " + superName + " " + (interfaces != null? Arrays.toString(interfaces): ""));
                        }

                        @Override
                        public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
                            //System.err.println("  component " + name + " " + ClassDesc.ofDescriptor(descriptor).displayName());
                            return null;
                        }

                        @Override
                        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                            //System.err.println("  field " + modifier(access) + " " + ClassDesc.ofDescriptor(descriptor).displayName());
                            return null;
                        }

                        @Override
                        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                            //System.err.println("\n method " + modifier(access) + " " + name + " " + MethodTypeDesc.ofDescriptor(descriptor).displayDescriptor() + " " + signature);
                            return new MethodVisitor(Opcodes.ASM9) {
                                private int lineNumber = -1;

                                @Override
                                public void visitInsn(int opcode) {
                                    var s = getOpcode(opcode);
                                    var opName = s.endsWith("RETURN") ? "RETURN" : s;
                                    map.computeIfAbsent(fileName, k -> new HashMap<>())
                                            .computeIfAbsent(lineNumber, k -> new StringJoiner("\n")).add(opName);
                                    //System.err.println("    visitInsn : " + getOpcode(opcode) + " | line " + lineNumber);
                                }

                                @Override
                                public void visitIntInsn(int opcode, int operand){
                                    map.computeIfAbsent(fileName, k -> new HashMap<>())
                                            .computeIfAbsent(lineNumber, k -> new StringJoiner("\n")).add(getOpcode(opcode));
                                    //System.err.println("    visitIntInsn : " + getOpcode(opcode) + " operand : " + operand + " | line " + lineNumber);
                                }

                                @Override
                                public void visitVarInsn(int opcode, int var){
                                    var s = getOpcode(opcode);
                                    var opName = s.endsWith("LOAD") ? "LOAD" : s.startsWith("V") ? "STORE" : s;
                                    map.computeIfAbsent(fileName, k -> new HashMap<>())
                                            .computeIfAbsent(lineNumber, k -> new StringJoiner("\n")).add(opName + " " + var);
                                    //System.err.println("    visitVarInsn : " + getOpcode(opcode) + " " + var + " | line " + lineNumber);
                                }

                                @Override
                                public void visitTypeInsn(int opcode, String desc){
                                    map.computeIfAbsent(fileName, k -> new HashMap<>())
                                            .computeIfAbsent(lineNumber, k -> new StringJoiner("\n")).add(getOpcode(opcode));
                                    //System.err.println("    visitTypeInsn : " + getOpcode(opcode)  + " | line " + lineNumber);
                                }

                                @Override
                                public void visitFieldInsn(int opc, String owner, String name, String desc){
                                    map.computeIfAbsent(fileName, k -> new HashMap<>())
                                            .computeIfAbsent(lineNumber, k -> new StringJoiner("\n")).add(getOpcode(opc));
                                    //System.err.println("    visitFieldInsn : " + getOpcode(opc) + " " + name);
                                }

                                @Override
                                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                                    var s = getOpcode(opcode);
                                    var opName = s.endsWith("SPECIAL") ? s : (s + " " + name);
                                    map.computeIfAbsent(fileName, k -> new HashMap<>())
                                            .computeIfAbsent(lineNumber, k -> new StringJoiner("\n")).add(opName);
                                    //System.err.println("    " + getOpcode(opcode) + " " + name + " | line " + lineNumber);
                                }

                                // + the other visit methods to get all the opcodes

                                @Override
                                public void visitIincInsn(int var, int increment){
                                    map.computeIfAbsent(fileName, k -> new HashMap<>())
                                            .computeIfAbsent(lineNumber, k -> new StringJoiner("\n")).add("INCREMENT " + increment);
                                    //System.err.println("visitIincInsn : " + var + " " + increment + " | line " + lineNumber);
                                }

                                @Override
                                public void visitJumpInsn(int opcode, Label label){
                                    map.computeIfAbsent(fileName, k -> new HashMap<>())
                                            .computeIfAbsent(lineNumber, k -> new StringJoiner("\n")).add(getOpcode(opcode));
                                    //System.err.println("    visitJumpInsn : " + getOpcode(opcode) + " " + label.toString()
                                         //   + " | line " + lineNumber);
                                }

                                @Override
                                public void visitLdcInsn(Object cst){
                                    map.computeIfAbsent(fileName, k -> new HashMap<>())
                                            .computeIfAbsent(lineNumber, k -> new StringJoiner("\n")).add("LDC " + cst.toString());
                                    //System.err.println("    visitLdcInsn : LDC " + cst.toString() + " | line " + lineNumber);
                                }

                                @Override
                                public void visitLineNumber(int line, Label start){
                                    this.lineNumber = line;
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
