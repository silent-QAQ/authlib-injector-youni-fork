package moe.yushi.authlibinjector.transform.support;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASM9;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.RETURN;
import java.util.Optional;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import moe.yushi.authlibinjector.transform.TransformContext;
import moe.yushi.authlibinjector.transform.TransformUnit;
import moe.yushi.authlibinjector.util.Logging;
import moe.yushi.authlibinjector.util.Logging.Level;

public class OnlineOptionsTransformer implements TransformUnit {

	@Override
	public Optional<ClassVisitor> transform(ClassLoader classLoader, String className, ClassVisitor writer, TransformContext ctx) {
		if ("net.minecraft.client.gui.screens.options.OnlineOptionsScreen".equals(className)) {
			Logging.log(Level.INFO, "[OnlineOptionsTransformer] Matched class: " + className);
			return Optional.of(new ClassVisitor(ASM9, writer) {
				@Override
				public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
					if ("confirmFriendsListEnabled".equals(name)
							&& "(Lnet/minecraft/client/Minecraft;Ljava/lang/Runnable;Lnet/minecraft/client/gui/screens/Screen;)V".equals(descriptor)) {
						ctx.markModified();
						Logging.log(Level.INFO, "[OnlineOptionsTransformer] HIT! Replacing confirmFriendsListEnabled → Runnable.run");

						MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
						mv.visitCode();
						mv.visitVarInsn(ALOAD, 1);
						mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/Runnable", "run", "()V", true);
						mv.visitInsn(RETURN);
						mv.visitMaxs(1, 3);
						mv.visitEnd();

						return null;
					}
					return super.visitMethod(access, name, descriptor, signature, exceptions);
				}
			});
		}
		return Optional.empty();
	}

	@Override
	public String toString() {
		return "OnlineOptions Transformer (confirmFriendsListEnabled → run)";
	}
}