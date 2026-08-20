package com.globaladhan.app.data.audio

import com.globaladhan.app.domain.audio.AudioAsset
import com.globaladhan.app.domain.audio.AudioProvider
import com.globaladhan.app.domain.audio.Muezzin
import com.globaladhan.app.domain.audio.Reciter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central audio orchestrator (spec §21). The UI depends only on this service;
 * providers are resolved by key and can be added/replaced via config.
 */
@Singleton
class AudioService @Inject constructor(
    demoProvider: DemoAudioProvider
) {

    /** Provider registry — add new providers here (config-driven, not UI-coupled). */
    private val providers: Map<String, AudioProvider> = mapOf(
        demoProvider.key to demoProvider
    )

    private val _activeProviderKey = MutableStateFlow("demo")
    val activeProviderKey: StateFlow<String> = _activeProviderKey.asStateFlow()

    val activeProvider: AudioProvider
        get() = providers[_activeProviderKey.value] ?: providers.values.first()

    suspend fun getReciters(): List<Reciter> = activeProvider.getReciters()

    suspend fun getMuezzins(): List<Muezzin> = activeProvider.getMuezzins()

    suspend fun getSurahAudio(surah: Int, reciterId: String): AudioAsset? =
        activeProvider.getSurahAudio(surah, reciterId)

    suspend fun getAyahAudio(surah: Int, ayah: Int, reciterId: String): AudioAsset? =
        activeProvider.getAyahAudio(surah, ayah, reciterId)

    suspend fun getAdhanAudio(muezzinId: String, isFajr: Boolean): AudioAsset? =
        activeProvider.getAdhanAudio(muezzinId, isFajr)

    suspend fun play(asset: AudioAsset) = activeProvider.play(asset)
    suspend fun pause() = activeProvider.pause()
    suspend fun resume() = activeProvider.resume()
    suspend fun stop() = activeProvider.stop()

    suspend fun download(asset: AudioAsset) = activeProvider.download(asset)
    suspend fun deleteDownload(assetId: String) = activeProvider.deleteDownload(assetId)
}
