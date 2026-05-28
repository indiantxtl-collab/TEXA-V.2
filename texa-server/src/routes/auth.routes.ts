import { Router } from 'express';
import { sendOtp, verifyOtp, getActiveSessions, terminateSession, registerDevice } from '../controllers/auth.controller';

const router = Router();

// Twilio core validation
router.post('/send-otp', sendOtp);
router.post('/verify-otp', verifyOtp);

// Multi-device session coordination
router.get('/sessions', getActiveSessions);
router.post('/sessions/register', registerDevice);
router.delete('/sessions/:id', terminateSession);

export default router;
