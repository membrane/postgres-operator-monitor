//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.predic8.pgopmon.crd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"apiVersion", "kind", "metadata", "description", "versions", "spec", "status"})
@JsonDeserialize()
public class Postgresql {
	@NotNull
	@JsonProperty("apiVersion")
	private String apiVersion = "postgresqls.acid.zalan.do/v1";
	@NotNull
	@JsonProperty("kind")
	private String kind = "postgresql";
	@JsonProperty("metadata")
	@Valid
	private V1ObjectMeta metadata;
	private Map<String, Object> spec;
	private Map<String, Object> status;

	public Postgresql() {
	}

	public Postgresql(String apiVersion, String kind, V1ObjectMeta metadata, Map<String, Object> spec, Map<String, Object> status) {
		this.apiVersion = apiVersion;
		this.kind = kind;
		this.metadata = metadata;
		this.spec = spec;
		this.status = status;
	}

	@JsonProperty("apiVersion")
	public String getApiVersion() {
		return this.apiVersion;
	}

	@JsonProperty("apiVersion")
	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	@JsonProperty("kind")
	public String getKind() {
		return this.kind;
	}

	@JsonProperty("kind")
	public void setKind(String kind) {
		this.kind = kind;
	}

	@JsonProperty("metadata")
	public V1ObjectMeta getMetadata() {
		return this.metadata;
	}

	@JsonProperty("metadata")
	public void setMetadata(V1ObjectMeta metadata) {
		this.metadata = metadata;
	}

	public String toString() {
		return "{\"kind\":\"SerializedReference\",\"apiVersion\":\"v1\",\"reference\":{\"kind\":\"postgresql\",\"namespace\":\"" + getMetadata().getNamespace() + "\",\"name\":\"" + getMetadata().getName() + "\",\"uid\":\"" + getMetadata().getUid() + "\",\"apiVersion\":\"" + getApiVersion() + "\",\"resourceVersion\":\"" + getMetadata().getResourceVersion() + "\"}}";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Postgresql postgresql = (Postgresql) o;

		if (!Objects.equals(apiVersion, postgresql.apiVersion)) return false;
		if (!Objects.equals(kind, postgresql.kind)) return false;
		if (!Objects.equals(metadata, postgresql.metadata)) return false;
		return Objects.equals(spec, postgresql.spec);
	}

	@Override
	public int hashCode() {
		int result = apiVersion != null ? apiVersion.hashCode() : 0;
		result = 31 * result + (kind != null ? kind.hashCode() : 0);
		result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
		result = 31 * result + (spec != null ? spec.hashCode() : 0);
		return result;
	}

	@JsonProperty("spec")
	public Map<String, Object> getSpec() {
		return spec;
	}

	@JsonProperty("spec")
	public void setSpec(Map<String, Object> spec) {
		this.spec = spec;
	}

	public Map<String, Object> getStatus() {
		return status;
	}

	public void setStatus(Map<String, Object> status) {
		this.status = status;
	}
}