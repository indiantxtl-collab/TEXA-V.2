package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TexaDao {

    // --- Messages ---
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: String)

    @Query("UPDATE messages SET reactions = :reaction WHERE id = :id")
    suspend fun updateMessageReaction(id: String, reaction: String)

    @Query("UPDATE messages SET isRead = 1 WHERE chatId = :chatId")
    suspend fun markChatAsRead(chatId: String)

    // --- Chats ---
    @Query("SELECT * FROM chats WHERE isArchived = 0 ORDER BY lastMessageTime DESC")
    fun getAllChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE isArchived = 1 ORDER BY lastMessageTime DESC")
    fun getArchivedChats(): Flow<List<ChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Query("UPDATE chats SET lastMessage = :lastMsg, lastMessageTime = :time WHERE id = :chatId")
    suspend fun updateLastMessage(chatId: String, lastMsg: String, time: Long)

    @Query("UPDATE chats SET selfDestructTimer = :seconds WHERE id = :chatId")
    suspend fun updateSelfDestructTimer(chatId: String, seconds: Long)

    @Query("UPDATE chats SET isArchived = :archived WHERE id = :chatId")
    suspend fun updateArchived(chatId: String, archived: Boolean)

    @Query("SELECT * FROM chats WHERE id = :chatId LIMIT 1")
    suspend fun getChatById(chatId: String): ChatEntity?

    // --- Communities ---
    @Query("SELECT * FROM communities ORDER BY membersCount DESC")
    fun getAllCommunities(): Flow<List<CommunityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunity(community: CommunityEntity)

    // --- Sessions ---
    @Query("SELECT * FROM sessions ORDER BY lastActive DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteSession(id: String)

    // --- Security Logs ---
    @Query("SELECT * FROM security_logs ORDER BY timestamp DESC LIMIT 20")
    fun getSecurityLogs(): Flow<List<SecurityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecurityLog(log: SecurityLogEntity)
}
