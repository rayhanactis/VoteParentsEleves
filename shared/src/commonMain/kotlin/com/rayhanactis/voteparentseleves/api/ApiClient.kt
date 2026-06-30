package com.rayhanactis.voteparentseleves.api

import com.rayhanactis.voteparentseleves.admin.CreationElecteur
import com.rayhanactis.voteparentseleves.admin.CreationListe
import com.rayhanactis.voteparentseleves.admin.CreationParent
import com.rayhanactis.voteparentseleves.admin.CreationScrutin
import com.rayhanactis.voteparentseleves.admin.ElecteurGenere
import com.rayhanactis.voteparentseleves.admin.GenerationElecteurs
import com.rayhanactis.voteparentseleves.admin.IdentifiantsGeneres
import com.rayhanactis.voteparentseleves.admin.ImportElecteurs
import com.rayhanactis.voteparentseleves.admin.ResultatEnvoiMasse
import com.rayhanactis.voteparentseleves.admin.LigneElecteurBrute
import com.rayhanactis.voteparentseleves.admin.MiseAJourParametres
import com.rayhanactis.voteparentseleves.admin.ModificationParent
import com.rayhanactis.voteparentseleves.admin.RenommageScrutin
import com.rayhanactis.voteparentseleves.admin.ResultatImport
import com.rayhanactis.voteparentseleves.model.Electeur
import com.rayhanactis.voteparentseleves.model.ParametresEcole
import com.rayhanactis.voteparentseleves.auth.DemandeLoginAdmin
import com.rayhanactis.voteparentseleves.auth.DemandeLoginElecteur
import com.rayhanactis.voteparentseleves.auth.ReponseToken
import com.rayhanactis.voteparentseleves.model.ListeCandidate
import com.rayhanactis.voteparentseleves.model.ParticipationScrutin
import com.rayhanactis.voteparentseleves.model.ResultatScrutin
import com.rayhanactis.voteparentseleves.model.Scrutin
import com.rayhanactis.voteparentseleves.vote.DemandeVote
import com.rayhanactis.voteparentseleves.vote.RecuVote
import com.rayhanactis.voteparentseleves.vote.StatutEmargement
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ApiClient(private val baseUrl: String) {

    private val http = creerHttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(Logging) { level = LogLevel.INFO }
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
        expectSuccess = false
    }

    private fun url(chemin: String) = "$baseUrl$chemin"


    suspend fun loginElecteur(req: DemandeLoginElecteur): ApiResult<ReponseToken> =
        appel { http.post(url("/auth/login")) { setBody(req) } }

    suspend fun loginAdmin(req: DemandeLoginAdmin): ApiResult<ReponseToken> =
        appel { http.post(url("/auth/admin/login")) { setBody(req) } }


    suspend fun listerScrutins(): ApiResult<List<Scrutin>> =
        appel { http.get(url("/scrutins")) }

    suspend fun lireScrutin(scrutinId: String): ApiResult<Scrutin> =
        appel { http.get(url("/scrutins/$scrutinId")) }

    suspend fun listerListes(scrutinId: String): ApiResult<List<ListeCandidate>> =
        appel { http.get(url("/scrutins/$scrutinId/listes")) }

    suspend fun lireResultats(scrutinId: String): ApiResult<ResultatScrutin> =
        appel { http.get(url("/scrutins/$scrutinId/resultats")) }


    suspend fun voter(
        token: String,
        scrutinId: String,
        demande: DemandeVote
    ): ApiResult<RecuVote> = appel {
        http.post(url("/scrutins/$scrutinId/voter")) {
            bearerAuth(token)
            setBody(demande)
        }
    }

    suspend fun aDejaVote(token: String, scrutinId: String): ApiResult<StatutEmargement> = appel {
        http.get(url("/scrutins/$scrutinId/mon-vote")) { bearerAuth(token) }
    }


    suspend fun creerScrutin(token: String, req: CreationScrutin): ApiResult<Scrutin> = appel {
        http.post(url("/scrutins")) {
            bearerAuth(token)
            setBody(req)
        }
    }

    suspend fun ajouterListe(
        token: String,
        scrutinId: String,
        req: CreationListe
    ): ApiResult<ListeCandidate> = appel {
        http.post(url("/scrutins/$scrutinId/listes")) {
            bearerAuth(token)
            setBody(req)
        }
    }

    suspend fun ouvrirScrutin(token: String, scrutinId: String): ApiResult<Scrutin> = appel {
        http.put(url("/scrutins/$scrutinId/ouvrir")) { bearerAuth(token) }
    }

    suspend fun fermerScrutin(token: String, scrutinId: String): ApiResult<Scrutin> = appel {
        http.put(url("/scrutins/$scrutinId/fermer")) { bearerAuth(token) }
    }

    suspend fun importerElecteurs(
        token: String,
        electeurs: List<CreationElecteur>
    ): ApiResult<ResultatImport> = appel {
        http.post(url("/admin/electeurs/import")) {
            bearerAuth(token)
            setBody(ImportElecteurs(electeurs))
        }
    }

    suspend fun genererElecteurs(
        token: String,
        lignes: List<LigneElecteurBrute>
    ): ApiResult<List<ElecteurGenere>> = appel {
        http.post(url("/admin/electeurs/generer")) {
            bearerAuth(token)
            setBody(GenerationElecteurs(lignes))
        }
    }

    suspend fun depouillerScrutin(token: String, scrutinId: String): ApiResult<Scrutin> = appel {
        http.put(url("/scrutins/$scrutinId/depouiller")) { bearerAuth(token) }
    }

    suspend fun participation(token: String, scrutinId: String): ApiResult<ParticipationScrutin> = appel {
        http.get(url("/scrutins/$scrutinId/participation")) { bearerAuth(token) }
    }

    suspend fun renommerScrutin(
        token: String,
        scrutinId: String,
        nouveauNom: String
    ): ApiResult<Scrutin> = appel {
        http.patch(url("/scrutins/$scrutinId")) {
            bearerAuth(token)
            setBody(RenommageScrutin(nouveauNom))
        }
    }

    suspend fun supprimerScrutin(token: String, scrutinId: String): ApiResult<Unit> = appel {
        http.delete(url("/scrutins/$scrutinId")) { bearerAuth(token) }
    }

    suspend fun supprimerListe(
        token: String,
        scrutinId: String,
        listeId: String
    ): ApiResult<Unit> = appel {
        http.delete(url("/scrutins/$scrutinId/listes/$listeId")) { bearerAuth(token) }
    }

    suspend fun modifierListe(
        token: String,
        scrutinId: String,
        listeId: String,
        req: CreationListe
    ): ApiResult<ListeCandidate> = appel {
        http.put(url("/scrutins/$scrutinId/listes/$listeId")) {
            bearerAuth(token)
            setBody(req)
        }
    }


    suspend fun listerParents(token: String): ApiResult<List<Electeur>> = appel {
        http.get(url("/admin/parents")) { bearerAuth(token) }
    }

    suspend fun lireParent(token: String, parentId: String): ApiResult<Electeur> = appel {
        http.get(url("/admin/parents/$parentId")) { bearerAuth(token) }
    }

    suspend fun creerParent(token: String, req: CreationParent): ApiResult<Electeur> = appel {
        http.post(url("/admin/parents")) {
            bearerAuth(token)
            setBody(req)
        }
    }

    suspend fun modifierParent(
        token: String,
        parentId: String,
        req: ModificationParent
    ): ApiResult<Electeur> = appel {
        http.put(url("/admin/parents/$parentId")) {
            bearerAuth(token)
            setBody(req)
        }
    }

    suspend fun supprimerParent(token: String, parentId: String): ApiResult<Unit> = appel {
        http.delete(url("/admin/parents/$parentId")) { bearerAuth(token) }
    }

    suspend fun genererMotDePasseParent(token: String, parentId: String): ApiResult<IdentifiantsGeneres> = appel {
        http.post(url("/admin/parents/$parentId/mot-de-passe")) { bearerAuth(token) }
    }

    suspend fun envoyerIdentifiants(token: String, parentId: String): ApiResult<IdentifiantsGeneres> = appel {
        http.post(url("/admin/parents/$parentId/envoyer")) { bearerAuth(token) }
    }

    suspend fun envoyerIdentifiantsTous(token: String): ApiResult<ResultatEnvoiMasse> = appel {
        http.post(url("/admin/parents/envoyer-tous")) { bearerAuth(token) }
    }


    suspend fun lireParametres(token: String): ApiResult<ParametresEcole> = appel {
        http.get(url("/admin/parametres")) { bearerAuth(token) }
    }

    suspend fun enregistrerParametres(
        token: String,
        req: MiseAJourParametres
    ): ApiResult<ParametresEcole> = appel {
        http.put(url("/admin/parametres")) {
            bearerAuth(token)
            setBody(req)
        }
    }


    private suspend inline fun <reified T> appel(
        bloc: () -> HttpResponse
    ): ApiResult<T> {
        val reponse = try {
            bloc()
        } catch (t: Throwable) {
            return ApiResult.Reseau(t)
        }
        return if (reponse.status.isSuccess()) {
            try {
                if (reponse.status == HttpStatusCode.NoContent) {
                    @Suppress("UNCHECKED_CAST")
                    ApiResult.Succes(Unit as T)
                } else {
                    ApiResult.Succes(reponse.body<T>())
                }
            } catch (t: Throwable) {
                ApiResult.Reseau(t)
            }
        } else {
            val msg = try { reponse.bodyAsText() } catch (_: Throwable) { "" }
            ApiResult.Echec(reponse.status.value, msg)
        }
    }
}
