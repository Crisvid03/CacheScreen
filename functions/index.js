const functions = require("firebase-functions");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");

admin.initializeApp();

// Configura tu cuenta de correo
const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: "cristian.play03@gmail.com",      // ← cambia esto
    pass: "wsgf fioe kbqv srlw"        // ← y esto (si usas Gmail, usa una App Password)
  }
});

exports.enviarPinCorreo = functions.https.onCall(async (data, context) => {
  const email = data.email;

  // Verificar si el correo está registrado
  const userRecord = await admin.auth().getUserByEmail(email).catch(() => null);
  if (!userRecord) {
    throw new functions.https.HttpsError('not-found', 'No existe una cuenta con ese correo.');
  }

  // Generar PIN de 6 dígitos
  const pin = Math.floor(100000 + Math.random() * 900000).toString();

  // Opcional: Guarda el PIN temporalmente en Firestore o Realtime Database

  // Enviar correo
  const mailOptions = {
    from: "ScreenSense <cristian.play03@gmail.com>", // <- Debe ser el mismo correo
    to: email,
    subject: "Tu código de verificación",
    text: `Tu PIN es: ${pin}`,
  };

  await transporter.sendMail(mailOptions);

  return { success: true, pin }; // puedes omitir el PIN aquí si solo se valida en el backend
});
