package fr.uge.clone;

import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.module.ModuleFinder;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.Arrays;

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
                            System.err.println("  field " + modifier(access) + " " + name + " " + ClassDesc.ofDescriptor(descriptor).displayName() + " " + signature);
                            return null;
                        }

                        @Override
                        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                            System.err.println("\n method " + modifier(access) + " " + name + " " + MethodTypeDesc.ofDescriptor(descriptor).displayDescriptor() + " " + signature);
                            return new MethodVisitor(Opcodes.ASM9) {
                                private int lineNumber = -1;

                                @Override
                                public void visitCode(){
                                    System.out.println("Start of the method's bytecode " + name + " : \n");
                                }

                                @Override
                                public void visitInsn(int opcode) {
                                    System.err.println("    visitInsn : " + getOpcode(opcode) + " | line " + lineNumber);
                                }

                                @Override
                                public void visitIntInsn(int opcode, int operand){
                                    System.err.println("    visitIntInsn : " + getOpcode(opcode) + " operand : " + operand);
                                }

                                @Override
                                public void visitVarInsn(int opcode, int var){
                                    System.err.println("    visitVarInsn : " + getOpcode(opcode) + " | line " + lineNumber);
                                }

                                @Override
                                public void visitTypeInsn(int opcode, String desc){
                                    System.err.println("    visitTypeInsn : " + getOpcode(opcode) + " | desc : " + desc);
                                }

                                @Override
                                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                                    System.err.println("    " + getOpcode(opcode)+ " " + owner+ " " + name + " " + descriptor + " | line " + lineNumber);
                                }

                                // + the other visit methods to get all the opcodes

                                @Override
                                public void visitIincInsn(int var, int increment){
                                    System.err.println("visitIincInsn : " + var);
                                }
                                @Override
                                public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index){
                                    System.err.println("    visitLocalVariable : " + name + " " + desc + " | line " + lineNumber);
                                }
                                @Override
                                public void visitLineNumber(int line, Label start){
                                    this.lineNumber = line;
                                }

                                @Override
                                public void visitMaxs(int maxStack, int maxLocals){

                                }
                                @Override
                                public void visitEnd(){
                                    System.out.println("\nEndEnd of the method's bytecode " + name + "\n");
                                }
                            };
                        }
                    }, 0);
                }
            }
        }
    }
}
