import express from "express";
import cors from "cors";
import nodemailer from "nodemailer";
import dotenv from "dotenv";

dotenv.config();

const app = express();
app.use(cors());
app.use(express.json());

app.post("/send", async (req, res) => {
  
  const { to, subject, message, replyTo } = req.body;

  try {
    const transporter = nodemailer.createTransport({
      service: "gmail",
      auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS,
      },
    });

    
    await transporter.sendMail({
      from: `"GoatFound" <${process.env.EMAIL_USER}>`,
      to,
      subject,
      text: message,
      replyTo,
    });

    console.log(`Correo enviado a ${to}, con replyTo ${replyTo}`);

    res.status(200).json({ success: true });
  } catch (err) {
    console.error("Error enviando correo:", err);
    res.status(500).json({ success: false, error: err.message });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Email service corriendo en puerto ${PORT}`));
