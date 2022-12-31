package fr.uge.clone.analyze;

import fr.uge.clone.model.OpcodeEntry;
import org.objectweb.asm.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
        return "";
    }


    public static Map<String, List<OpcodeEntry>> parse(InputStream input) throws IOException {
        final Map<String, List<OpcodeEntry>> map2 = new HashMap<>();

        try(var zip = new ZipInputStream(input)){
            ZipEntry e;
            while ((e = zip.getNextEntry()) != null) {
                if (!e.getName().endsWith(".class") || e.getName().contains("$")) {
                    continue;
                }

                        try(var inputStream = new ByteArrayInputStream(zip.readAllBytes())) {
                            var classReader = new ClassReader(inputStream);
                            classReader.accept(new ClassVisitor(Opcodes.ASM9) {
                                String fileName;

                                @Override
                                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                                    fileName = name;
                                }

                                @Override
                                public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
                                    return null;
                                }

                                @Override
                                public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                                    return null;
                                }

                                @Override
                                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                                    return new MethodVisitor(Opcodes.ASM9) {
                                        private int lineNumber = -1;

                                        @Override
                                        public void visitInsn(int opcode) {
                                            var s = getOpcode(opcode);
                                            var op = s.endsWith("RETURN") ? Opcodes.RETURN : opcode;

                                            map2.computeIfAbsent(fileName, k -> new ArrayList<>()).add(new OpcodeEntry(op, lineNumber));
                                        }

                                        @Override
                                        public void visitIntInsn(int opcode, int operand){

                                            map2.computeIfAbsent(fileName, k -> new ArrayList<>()).add(new OpcodeEntry(opcode, lineNumber));
                                            map2.computeIfAbsent(fileName, k -> new ArrayList<>()).add(new OpcodeEntry(operand, lineNumber));
                                        }

                                        @Override
                                        public void visitVarInsn(int opcode, int var){
                                            var s = getOpcode(opcode);
                                            var op = s.endsWith("LOAD") ? Opcodes.ALOAD : s.startsWith("V") ? Opcodes.V10 : opcode;
                                            map2.computeIfAbsent(fileName, k -> new ArrayList<>()).add(new OpcodeEntry(op, lineNumber));
                                            map2.computeIfAbsent(fileName, k -> new ArrayList<>()).add(new OpcodeEntry(var, lineNumber));
                                        }

                                        @Override
                                        public void visitTypeInsn(int opcode, String desc){
                                            map2.computeIfAbsent(fileName, k -> new ArrayList<>()).add(new OpcodeEntry(opcode, lineNumber));
                                        }

                                        @Override
                                        public void visitFieldInsn(int opc, String owner, String name, String desc){
                                            map2.computeIfAbsent(fileName, k -> new ArrayList<>()).add(new OpcodeEntry(opc, lineNumber));
                                        }

                                        @Override
                                        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                                            var s = getOpcode(opcode);
                                            map2.computeIfAbsent(fileName, k -> new ArrayList<>()).add(new OpcodeEntry(opcode, lineNumber));
                                            if(s.endsWith("STATIC") || s.endsWith("SPECIAL")){
                                                map2.computeIfAbsent(fileName, k -> new ArrayList<>()).add(new OpcodeEntry(name.hashCode(), lineNumber));
                                            }
                                        }


                                        @Override
                                        public void visitIincInsn(int var, int increment){
                                            map2.computeIfAbsent(fileName, k -> new ArrayList<>()).add(new OpcodeEntry(var, lineNumber));
                                            map2.computeIfAbsent(fileName, k -> new ArrayList<>()).add(new OpcodeEntry(increment, lineNumber));
                                        }

                                        @Override
                                        public void visitJumpInsn(int opcode, Label label){
                                            map2.computeIfAbsent(fileName, k -> new ArrayList<>()).add(new OpcodeEntry(opcode, lineNumber));
                                        }

                                        @Override
                                        public void visitLdcInsn(Object cst){
                                            map2.computeIfAbsent(fileName, k -> new ArrayList<>()).add(new OpcodeEntry(Opcodes.LDC, lineNumber));
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return map2;
    }

}
