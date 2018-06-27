package com.square.renov.swipevoicechat.Network.network;

public interface IRequest {

	void execute();

	void cancel();

	boolean isFinished();
}
