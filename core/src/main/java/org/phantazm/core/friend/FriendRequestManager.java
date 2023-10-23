package org.phantazm.core.friend;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;

import java.util.*;

public class FriendRequestManager {

    private final Map<UUID, Set<UUID>> outgoingRequests = new HashMap<>();

    private final Map<UUID, Set<UUID>> incomingRequests = new HashMap<>();

    private final Queue<FriendRequest> requests = new LinkedList<>();

    private final FriendDatabase friendDatabase;

    private final FriendNotification notification;

    private final long requestDuration;

    private long ticks = 0L;

    public FriendRequestManager(@NotNull FriendDatabase friendDatabase, @NotNull FriendNotification notification,
            long requestDuration) {
        this.friendDatabase = Objects.requireNonNull(friendDatabase);
        this.notification = Objects.requireNonNull(notification);
        this.requestDuration = requestDuration;
    }

    public void tick() {
        ++ticks;

        FriendRequest request = requests.peek();
        while (request != null && request.expirationTime <= ticks) {
            Set<UUID> outgoing = outgoingRequests.get(request.requester().getUUID());
            outgoing.remove(request.target().getUUID());
            if (outgoing.isEmpty()) {
                outgoingRequests.remove(request.requester().getUUID());
            }

            Set<UUID> incoming = incomingRequests.get(request.target().getUUID());
            incoming.remove(request.requester().getUUID());
            if (incoming.isEmpty()) {
                incomingRequests.remove(request.target().getUUID());
            }

            notification.notifyExpiry(request.requester(), request.target());

            requests.remove();
            request = requests.peek();
        }
    }

    public void sendRequest(@NotNull PlayerView requester, @NotNull PlayerView target) {
        if (requestDuration == 0) {
            return;
        }

        FriendRequest request = new FriendRequest(requester, target, ticks + requestDuration);
        requests.add(request);
        incomingRequests.computeIfAbsent(target.getUUID(), unused -> new HashSet<>()).add(requester.getUUID());
        outgoingRequests.computeIfAbsent(requester.getUUID(), unused -> new HashSet<>()).add(target.getUUID());

        notification.notifyRequest(requester, target);
    }

    public void acceptRequest(@NotNull PlayerView requester, @NotNull PlayerView target) {
        Iterator<FriendRequest> requestIterator = requests.iterator();
        while (requestIterator.hasNext()) {
            FriendRequest request = requestIterator.next();
            if (request.requester().getUUID().equals(requester.getUUID()) && request.target().getUUID().equals(target.getUUID())) {
                requestIterator.remove();
                incomingRequests.get(target.getUUID()).remove(requester.getUUID());
                outgoingRequests.get(requester.getUUID()).remove(target.getUUID());

                friendDatabase.addFriend(target.getUUID(), requester.getUUID());
                notification.notifyAccept(requester, target);
            }
        }
    }

    public boolean hasOutgoingRequest(@NotNull UUID requesterUUID, @NotNull UUID targetUUID) {
        Set<UUID> outgoing = outgoingRequests.get(requesterUUID);
        if (outgoing == null) {
            return false;
        }

        return outgoing.contains(targetUUID);
    }

    public boolean hasIncomingRequest(@NotNull UUID requesterUUID, @NotNull UUID targetUUID) {
        Set<UUID> incoming = incomingRequests.get(targetUUID);
        if (incoming == null) {
            return false;
        }

        return incoming.contains(requesterUUID);
    }

    private record FriendRequest(PlayerView requester, PlayerView target, long expirationTime) {

    }

}
