package com.example.deeplinkdemo

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.net.toUri
import com.example.deeplinkdemo.databinding.ActivityMainBinding
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ktx.androidParameters

class MainActivity : AppCompatActivity() {

    var binding: ActivityMainBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //       setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        handleIncomingDeepLinks()

        binding?.tvHello?.setOnClickListener {
            generateSharingLink(
                deepLink = "${Constants.PREFIX}/post/1".toUri(),
                previewImageLink = "https://cdn.corporatefinanceinstitute.com/assets/products-and-services-1024x1024.jpeg".toUri()
            ) { generatedLink ->
                // Use this generated Link to share via Intent
                shareDeepLink(generatedLink)
            }

        }
    }

    private fun handleIncomingDeepLinks() {

        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                if (pendingDynamicLinkData != null) {
                    val deepLink = pendingDynamicLinkData.link
                    deepLink?.let { uri ->
                        val path =
                            uri.toString().substring(deepLink.toString().lastIndexOf("/") + 1)

                        // In case if you have multiple shareable items such as User Post, User Profile,
                        // you can check if
                        // the uri contains the required string.
                        // In our case we will check if the path contains the string, 'post'

                        when {
                            uri.toString().contains("post") -> {
                                // In my case, the ID is an Integer
                                val postId = path.toInt()
                                // Call your API or DB to get the post with the ID [postId]
                                // and open the required screen here.
                                println("postId  $postId")
                            }
                        }
                    }
                }else{

                }
                }.addOnFailureListener {

                    Log.d("TAG", "handleIncomingDeepLinks: ${it.message}")
                }

    }

    private fun shareDeepLink(generatedLink: String) {

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "You have been shared an amazing meme, check it out ->")
        intent.putExtra(Intent.EXTRA_TEXT, generatedLink)
        startActivity(intent)

    }

    fun generateSharingLink(
        deepLink: Uri,
        previewImageLink: Uri,
        getShareableLink: (String) -> Unit = {},
    ) {
        FirebaseDynamicLinks.getInstance().createDynamicLink().run {
            // What is this link parameter? You will get to know when we will actually use this function.
            link = deepLink

            // [domainUriPrefix] will be the domain name you added when setting up Dynamic Links at Firebase Console.
            // You can find it in the Dynamic Links dashboard.
            domainUriPrefix = Constants.PREFIX

            // Pass your preview Image Link here;
            setSocialMetaTagParameters(
                DynamicLink.SocialMetaTagParameters.Builder()
                    .setImageUrl(previewImageLink)
                    .build()
            )

            // Required
            androidParameters {
                build()
            }

            // Finally
            buildShortDynamicLink()
        }.also {
            it.addOnSuccessListener { dynamicLink ->
                // This lambda will be triggered when short link generation is successful

                // Retrieve the newly created dynamic link so that we can use it further for sharing via Intent.
                getShareableLink.invoke(dynamicLink.shortLink.toString())
            }
            it.addOnFailureListener {
                // This lambda will be triggered when short link generation failed due to an exception

                // Handle
            }
        }
    }

}