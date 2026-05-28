package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String,
    val encryptedContent: String, // Hex or Base64 AES-GCM string
    val decryptedPreview: String, // Only decrypted in-memory, but saved as AES-GCM simulated key in database
    val timestamp: Long,
    val isRead: Boolean = false,
    val disappearingDuration: Long = 0, // 0 means permanent
    val selfDestructTimestamp: Long = 0,
    val reactions: String = "", // Comma-separated emojis e.g. "👍,🔥"
    val isStarred: Boolean = false,
    val type: String = "TEXT", // TEXT, IMAGE, VIDEO, AUDIO, VOICENOTE, DOCUMENT
    val mediaUrl: String? = null,
    val mediaDuration: Int = 0, // for audio/video
    val replyToId: String? = null,
    val replyToPreview: String? = null
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isGroup: Boolean = false,
    val avatarUrl: String,
    val lastMessage: String = "",
    val lastMessageTime: Long = 0,
    val unreadCount: Int = 0,
    val isLocked: Boolean = false,
    val selfDestructTimer: Long = 0, // disappearing timer setting in seconds
    val activeSessionKeySignature: String = "", // Ed25519 E2E Signature
    var isArchived: Boolean = false
)

@Entity(tableName = "communities")
data class CommunityEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val bannerUrl: String,
    val membersCount: Int,
    val role: String = "MEMBER", // ADMIN, MODERATOR, MEMBER, VISITOR
    val isPublic: Boolean = true,
    val unreadCount: Int = 0
)

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val deviceName: String,
    val location: String,
    val lastActive: Long,
    val isCurrent: Boolean = false,
    val sessionKeyFingerprint: String // Hex string representing the session signature
)

@Entity(tableName = "security_logs")
data class SecurityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String, // KEY_ROTATION, SCREENSHOT_BLOCKED, PACKET_SECURED, BIO_AUTH, E2E_HANDSHAKE
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val payloadDigest: String // Simulating cryptographic digests of secure frames
)
