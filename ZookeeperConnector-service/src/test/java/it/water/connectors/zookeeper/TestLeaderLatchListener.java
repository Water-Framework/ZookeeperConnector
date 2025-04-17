package it.water.connectors.zookeeper;

import lombok.Getter;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;

public class TestLeaderLatchListener implements LeaderLatchListener {
    @Getter
    private boolean pathLeader = false;

    @Override
    public void isLeader() {
        this.pathLeader = true;
    }

    @Override
    public void notLeader() {
        this.pathLeader = false;
    }
}
