export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") return cors(new Response(null, { status: 204 }));

    const url = new URL(request.url);
    const path = url.pathname.replace(/\/+$/, "");

    try {
      if (request.method === "POST" && path === "/sessions") {
        const code = createCode();
        const id = env.LISTEN_TOGETHER_SESSION.idFromName(code);
        return env.LISTEN_TOGETHER_SESSION.get(id).fetch(new Request(`${url.origin}/session/${code}/create`, request));
      }

      const match = path.match(/^\/sessions\/([A-Z0-9]{4,12})(?:\/(join|state|leave))?$/i);
      if (!match) {
        return json({ ok: true, service: "AirBeats Listen Together" });
      }

      const code = match[1].toUpperCase();
      const action = match[2] || "read";
      const id = env.LISTEN_TOGETHER_SESSION.idFromName(code);
      return env.LISTEN_TOGETHER_SESSION.get(id).fetch(new Request(`${url.origin}/session/${code}/${action}`, request));
    } catch (error) {
      return json({ error: error.message || "Server error" }, 500);
    }
  },
};

export class ListenTogetherSession {
  constructor(state, env) {
    this.state = state;
    this.env = env;
  }

  async fetch(request) {
    if (request.method === "OPTIONS") return cors(new Response(null, { status: 204 }));

    const url = new URL(request.url);
    const parts = url.pathname.split("/").filter(Boolean);
    const code = parts[1];
    const action = parts[2];

    let session = (await this.state.storage.get("session")) || null;

    if (request.method === "POST" && action === "create") {
      const body = await request.json();
      const participantId = crypto.randomUUID();
      session = {
        code,
        hostId: participantId,
        hostName: cleanName(body.name),
        controllerId: participantId,
        controllerName: cleanName(body.name),
        stateVersion: 1,
        state: sanitizeState(body.state),
        participants: [{ id: participantId, name: cleanName(body.name), joinedAt: Date.now() }],
        updatedAt: Date.now(),
      };
      await this.save(session);
      return this.sessionResponse(session, participantId, request);
    }

    if (!session) return json({ error: "Session not found" }, 404);

    if (request.method === "GET" && action === "read") {
      return this.sessionResponse(session, "", request);
    }

    if (request.method === "POST" && action === "join") {
      const body = await request.json();
      const participantId = crypto.randomUUID();
      session.participants = [
        ...session.participants,
        { id: participantId, name: cleanName(body.name), joinedAt: Date.now() },
      ].slice(-50);
      await this.save(session);
      return this.sessionResponse(session, participantId, request);
    }

    if (request.method === "POST" && action === "leave") {
      const body = await request.json();
      const participantId = String(body.participantId || "");
      if (!participantId) return json({ error: "Missing participantId" }, 400);

      if (participantId === session.hostId) {
        await this.state.storage.deleteAll();
        return json({ left: true, ended: true });
      }

      session.participants = session.participants.filter((participant) => participant.id !== participantId);
      session.updatedAt = Date.now();
      await this.save(session);
      return this.sessionResponse(session, participantId, request);
    }

    if (request.method === "POST" && action === "state") {
      const body = await request.json();
      const participantId = String(body.participantId || "");
      const participant = session.participants.find((item) => item.id === participantId);
      if (!participant) return json({ error: "Join the session before controlling playback" }, 403);
      session.state = sanitizeState(body.state);
      session.controllerId = participantId;
      session.controllerName = participant.name;
      session.stateVersion = Number(session.stateVersion || 0) + 1;
      session.updatedAt = Date.now();
      await this.save(session);
      return this.sessionResponse(session, participantId, request);
    }

    return json({ error: "Not found" }, 404);
  }

  async save(session) {
    await this.state.storage.put("session", session);
  }

  sessionResponse(session, participantId, request) {
    const origin = new URL(request.url).origin;
    return json({
      code: session.code,
      participantId,
      joinUrl: `${origin}/join/${session.code}`,
      participants: session.participants.length,
      participantList: session.participants.map((participant) => ({
        id: participant.id,
        name: participant.name,
        isHost: participant.id === session.hostId,
      })),
      hostName: session.hostName || session.participants.find((participant) => participant.id === session.hostId)?.name || "AirBeats listener",
      controllerId: session.controllerId || session.hostId,
      controllerName: session.controllerName || session.hostName || "AirBeats listener",
      stateVersion: Number(session.stateVersion || 1),
      serverNow: Date.now(),
      state: session.state,
      updatedAt: session.updatedAt,
    });
  }
}

function createCode() {
  const alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  const bytes = new Uint8Array(6);
  crypto.getRandomValues(bytes);
  return Array.from(bytes, (byte) => alphabet[byte % alphabet.length]).join("");
}

function sanitizeState(state = {}) {
  if (!state.songId) throw new Error("Missing songId");
  return {
    songId: String(state.songId).slice(0, 64),
    title: String(state.title || "Unknown title").slice(0, 180),
    artists: Array.isArray(state.artists) ? state.artists.map((it) => String(it).slice(0, 120)).slice(0, 8) : [],
    thumbnailUrl: state.thumbnailUrl ? String(state.thumbnailUrl).slice(0, 600) : null,
    positionMs: Math.max(0, Number(state.positionMs || 0)),
    isPlaying: Boolean(state.isPlaying),
    updatedAt: Date.now(),
  };
}

function cleanName(name) {
  return String(name || "AirBeats listener").trim().slice(0, 48) || "AirBeats listener";
}

function json(body, status = 200) {
  return cors(
    new Response(JSON.stringify(body), {
      status,
      headers: { "Content-Type": "application/json; charset=utf-8" },
    }),
  );
}

function cors(response) {
  const headers = new Headers(response.headers);
  headers.set("Access-Control-Allow-Origin", "*");
  headers.set("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
  headers.set("Access-Control-Allow-Headers", "Content-Type,Authorization");
  return new Response(response.body, { status: response.status, statusText: response.statusText, headers });
}
