package run.halo.app.content;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link ContentRequest}.
 *
 * @author guqing
 * @since 2.0.0
 */
class ContentRequestTest {
    private ContentRequest contentRequest;

    @BeforeEach
    void setUp() {
        Snapshot.SubjectRef subjectRef = Snapshot.SubjectRef.of(Post.KIND, "test-post");
        contentRequest = new ContentRequest(subjectRef, "snapshot-1", """
            Four score and seven
            years ago our fathers

            brought forth on this continent
            """,
            """
                <p>Four score and seven</p>
                <p>years ago our fathers</p>
                <br/>
                <p>brought forth on this continent</p>
                """,
            "MARKDOWN");
    }

    @Test
    void toSnapshot() throws JSONException {
        String expectedContentPath =
            "<p>Four score and seven</p>\n<p>years ago our fathers</p>\n<br/>\n<p>brought forth "
                + "on this continent</p>\n";
        String expectedRawPatch =
            "Four score and seven\nyears ago our fathers\n\nbrought forth on this continent\n";
        Snapshot snapshot = contentRequest.toSnapshot();
        snapshot.getMetadata().setName("7b149646-ac60-4a5c-98ee-78b2dd0631b2");
        JSONAssert.assertEquals(JsonUtils.objectToJson(snapshot),
            """
                {
                    "spec": {
                        "subjectRef": {
                            "kind": "Post",
                            "name": "test-post"
                        },
                        "rawType": "MARKDOWN",
                        "rawPatch": "%s",
                        "contentPatch": "%s",
                        "parentSnapshotName": null,
                        "displayVersion": "v1",
                        "version": 1,
                        "publishTime": null,
                        "contributors": null
                    },
                    "apiVersion": "content.halo.run/v1alpha1",
                    "kind": "Snapshot",
                    "metadata": {
                        "name": "7b149646-ac60-4a5c-98ee-78b2dd0631b2",
                        "labels": null,
                        "annotations": null,
                        "version": null,
                        "creationTimestamp": null,
                        "deletionTimestamp": null
                    }
                }
                """.formatted(expectedRawPatch, expectedContentPath),
            true);
    }

    @Test
    void rawPatchFrom() throws JSONException {
        String s = contentRequest.rawPatchFrom("""
            Four score and seven
            years ago our fathers
            """);
        JSONAssert.assertEquals(s,
            """
                   [
                    {
                        "source": {
                            "position": 3,
                            "lines": [],
                            "changePosition": null
                        },
                        "target": {
                            "position": 3,
                            "lines": [
                                "brought forth on this continent",
                                ""
                            ],
                            "changePosition": null
                        },
                        "type": "INSERT"
                    }
                ]
                """, true);
    }

    @Test
    void contentPatchFrom() throws JSONException {
        String s = contentRequest.contentPatchFrom("""
            <p>Four score and seven</p>
            <p>years ago our fathers</p>
            """);
        JSONAssert.assertEquals(s, """
            [
                 {
                     "source": {
                         "position": 2,
                         "lines": [],
                         "changePosition": null
                     },
                     "target": {
                         "position": 2,
                         "lines": [
                             "<br/>",
                             "<p>brought forth on this continent</p>"
                         ],
                         "changePosition": null
                     },
                     "type": "INSERT"
                 }
            ]
            """, true);
    }
}