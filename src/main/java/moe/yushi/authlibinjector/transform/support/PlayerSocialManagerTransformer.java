package moe.yushi.authlibinjector.transform.support;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASM9;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import java.util.Optional;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import moe.yushi.authlibinjector.transform.TransformContext;
import moe.yushi.authlibinjector.transform.TransformUnit;
import moe.yushi.authlibinjector.util.Logging;
import moe.yushi.authlibinjector.util.Logging.Level;

public class PlayerSocialManagerTransformer implements TransformUnit {

	private static final String CLASS_NAME = "net.minecraft.client.gui.screens.social.PlayerSocialManager";
	private static final String OWNER = "net/minecraft/client/gui/screens/social/PlayerSocialManager";
	private static final String REMOTE_HANDLER = "net/minecraft/client/gui/screens/social/RemoteFriendListUpdateHandler";

	@Override
	public Optional<ClassVisitor> transform(ClassLoader classLoader, String className, ClassVisitor writer, TransformContext ctx) {
		if (CLASS_NAME.equals(className)) {
			Logging.log(Level.INFO, "[PlayerSocialManagerTransformer] Matched class: " + className);
			return Optional.of(new ClassVisitor(ASM9, writer) {
				@Override
				public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
					if ("isFriendListEnabled".equals(name) && "()Z".equals(descriptor)) {
						ctx.markModified();
						Logging.log(Level.INFO, "[PlayerSocialManagerTransformer] HIT! Replacing isFriendListEnabled → true");

						MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
						mv.visitCode();
						mv.visitInsn(ICONST_1);
						mv.visitInsn(IRETURN);
						mv.visitMaxs(1, 1);
						mv.visitEnd();

						return null;
					}

					if ("setFriendListEnabled".equals(name) && "(Z)V".equals(descriptor)) {
						ctx.markModified();
						Logging.log(Level.INFO, "[PlayerSocialManagerTransformer] HIT! Replacing setFriendListEnabled → always true + start");

						MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
						mv.visitCode();
						mv.visitVarInsn(ALOAD, 0);
						mv.visitInsn(ICONST_1);
						mv.visitFieldInsn(PUTFIELD, OWNER, "friendListEnabled", "Z");
						mv.visitVarInsn(ALOAD, 0);
						mv.visitFieldInsn(GETFIELD, OWNER, "remoteFriendListUpdateHandler", "L" + REMOTE_HANDLER + ";");
						mv.visitMethodInsn(INVOKEVIRTUAL, REMOTE_HANDLER, "start", "()V", false);
						mv.visitInsn(RETURN);
						mv.visitMaxs(2, 2);
						mv.visitEnd();

						return null;
					}

					MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
					if ("<init>".equals(name)) {
						return new MethodVisitor(ASM9, mv) {
							@Override
							public void visitInsn(int opcode) {
								if (opcode == RETURN) {
									visitVarInsn(ALOAD, 0);
									visitInsn(ICONST_1);
									visitFieldInsn(PUTFIELD, OWNER, "friendListEnabled", "Z");
									visitVarInsn(ALOAD, 0);
									visitFieldInsn(GETFIELD, OWNER, "remoteFriendListUpdateHandler", "L" + REMOTE_HANDLER + ";");
									visitMethodInsn(INVOKEVIRTUAL, REMOTE_HANDLER, "start", "()V", false);
									ctx.markModified();
									Logging.log(Level.INFO, "[PlayerSocialManagerTransformer] HIT! Constructor enables friend list");
								}
								super.visitInsn(opcode);
							}
						};
					}
					return mv;
				}
			});
		}
		return Optional.empty();
	}

	@Override
	public String toString() {
		return "PlayerSocialManager Transformer (friends enabled)";
	}
}