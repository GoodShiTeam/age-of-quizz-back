package goodshi.ageofquizz.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

@Service
public class SftpService {

	@Value("${remote.server.url}")
	private String host;

	@Value("${remote.server.user}")
	private String username;

	@Value("${remote.server.password}")
	private String password;

	public void upload(InputStream inputStream, String remotePath) throws Exception {

		JSch jsch = new JSch();
		Session session = jsch.getSession(username, host);
		session.setPassword(password);

		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);

		session.connect();

		Channel channel = session.openChannel("sftp");
		channel.connect();

		ChannelSftp sftp = (ChannelSftp) channel;
		sftp.put(inputStream, remotePath);

		sftp.exit();
		session.disconnect();
	}

	public InputStream download(String remotePath) throws Exception {
		JSch jsch = new JSch();
		Session session = jsch.getSession(username, host);
		session.setPassword(password);

		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);

		session.connect();

		Channel channel = session.openChannel("sftp");
		channel.connect();

		ChannelSftp sftp = (ChannelSftp) channel;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		sftp.get(remotePath, baos);

		sftp.exit();
		session.disconnect();

		return new ByteArrayInputStream(baos.toByteArray());
	}
}
