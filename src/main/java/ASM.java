import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.module.ModuleFinder;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.Arrays;

public class ASM {

    private static String getTypeName(String type) {
        var arr = type.split("/");
        return arr[arr.length - 1];
    }

    private static String getNameFromOpcode(int opcode) {
        var fields = Opcodes.class.getDeclaredFields();
        try {
            for (var field : fields){
                if((int) field.get(field) == opcode){
                    return field.getName();
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        return null;
    }

    public static void main(String[] args) throws IOException {

        var finder = ModuleFinder.of(Path.of("testt.jar"));
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

                        /*************************************************************/

                        @Override
                        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value){
                            System.err.println("field : " +  name + " type : " + getTypeName(desc));
                            return new FieldVisitor(Opcodes.ASM9) {
                                @Override
                                public void visitAttribute(Attribute attr){
                                    System.err.println("visitAttribute");
                                }
                            };
                        }

                        /**************************************************************/

                        @Override
                        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                            System.err.println("\n  METHOD " + modifier(access) + " " + name + " " + MethodTypeDesc.ofDescriptor(descriptor).displayDescriptor());
                            return new MethodVisitor(Opcodes.ASM9) {

                                @Override
                                public void visitCode(){
                                    System.err.println("VISIT CODE OF METHOD " + name);
                                }


                                @Override
                                public void visitInsn(int opcode) {
                                    System.err.println("    opcode " + getNameFromOpcode(opcode));
                                }

                                @Override
                                public void visitIntInsn(int opcode, int operand){
                                    System.err.println(" VISIT INT INSN : " + operand);
                                }

                                @Override
                                public void visitVarInsn(int opcode, int var){
                                    System.err.println("visitVarInsn : " + var);
                                }

                                @Override
                                public void visitTypeInsn(int opcode, String desc){
                                    System.err.println("visiteTypeInsn " + getTypeName(desc));
                                }

                                @Override
                                public void visitFieldInsn(int opc, String owner, String name, String desc){
                                    System.err.println("Field instr : " + name + " type : " + getTypeName(desc) + " owner : " + getTypeName(desc));
                                }

                                @Override
                                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                                    System.err.println("    opcode " + getNameFromOpcode(opcode) + " " + getTypeName(name));
                                    System.err.println();
                                }

                                @Override
                                public void visitJumpInsn(int opcode, Label label){

                                }

                                @Override
                                public void visitLabel(Label label){

                                }

                                @Override
                                public void visitLdcInsn(Object cst){

                                }

                                @Override
                                public void visitIincInsn(int var, int increment){

                                }

                                @Override
                                public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels){

                                }

                                @Override
                                public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels){

                                }

                                @Override
                                public void visitMultiANewArrayInsn(String desc, int dims){

                                }

                                @Override
                                public void visitTryCatchBlock(Label start, Label end, Label handler, String type){

                                }

                                @Override
                                public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index){

                                }

                                /*void visitFrame(int type, int nLocal, Object[] local, int nStack,
                                                Object[] stack);
                                void visitInsn(int opcode);
                                void visitIntInsn(int opcode, int operand);
                                void visitVarInsn(int opcode, int var);
                                void visitTypeInsn(int opcode, String desc);
                                void visitFieldInsn(int opc, String owner, String name, String desc);
                                void visitMethodInsn(int opc, String owner, String name, String desc);
                                void visitInvokeDynamicInsn(String name, String desc, Handle bsm,
                                                            Object... bsmArgs);
                                void visitJumpInsn(int opcode, Label label);
                                void visitLabel(Label label);
                                void visitLdcInsn(Object cst);
                                void visitIincInsn(int var, int increment);
                                void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels);
                                void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels);
                                void visitMultiANewArrayInsn(String desc, int dims);
                                void visitTryCatchBlock(Label start, Label end, Label handler,
                                                        String type);
                                void visitLocalVariable(String name, String desc, String signature,
                                                        Label start, Label end, int index);
                                void visitLineNumber(int line, Label start);
                                void visitMaxs(int maxStack, int maxLocals);
                                void visitEnd();*/

                                @Override
                                public void visitLineNumber(int line, Label start){
                                    System.out.println("\n LINE NUMBER : " + line + " " + start + "\n\n");
                                }

                                @Override
                                public void visitEnd(){
                                    System.err.println("        End of method " + name);
                                }

                            };
                        }

                    }, 0);
                }
            }
        }
    }
}
