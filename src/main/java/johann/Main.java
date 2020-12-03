//package org.hyperledger.fabric.sdkintegration;
package johann;

import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import com.google.protobuf.ByteString;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.hyperledger.fabric.protos.msp.MspConfig.IdemixMSPSignerConfig;
import org.hyperledger.fabric.sdk.idemix.IdemixCredRequest;
import org.hyperledger.fabric.sdk.idemix.IdemixCredential;
import org.hyperledger.fabric.protos.msp.Identities.SerializedIdemixIdentity;
import org.hyperledger.fabric.protos.idemix.Idemix.Credential;
import org.hyperledger.fabric.protos.idemix.Idemix;
import org.hyperledger.fabric.sdk.idemix.IdemixUtils;
import org.json.JSONObject;

import org.apache.milagro.amcl.FP256BN.BIG;
import org.apache.milagro.amcl.FP256BN.ECP;
import org.apache.milagro.amcl.FP256BN.ECP2;
import org.apache.milagro.amcl.FP256BN.PAIR;
import org.apache.milagro.amcl.RAND;

/*
    This runs a version of end2end but with Node chaincode.
    It requires that End2endIT has been run already to do all enrollment and setting up of orgs,
    creation of the channels. None of that is specific to chaincode deployment language.
 */

//johann

public class Main {


    public static void main(String[] args) throws Exception {
        Options options = new Options();

        Option input = new Option("i", "input", true, "input path to JSon IDEMIX Hyperledger SignerConfig file");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("o", "output", true, "output path to JSon IDEMIX Hyperledger SignerConfig file");
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.out.println("IF no output is provided, than OUTPUT_PATH = INPUT_PATH");

            System.exit(1);
        }

        String inputFilePath = cmd.getOptionValue("input");
        String outputFilePath = cmd.getOptionValue("output");
        if (outputFilePath == null)
            outputFilePath=inputFilePath;

        System.out.println(inputFilePath);
        System.out.println(outputFilePath);
        
        String pathSignerConfig = inputFilePath;
        String signerConfigJson = Files.readString(Paths.get(pathSignerConfig));
        //System.out.println(signerConfigJson);
        
        JSONObject obj = new JSONObject(signerConfigJson);

        String cred = obj.getString("Cred");
        System.out.println("Cred: "+cred);
        String sk = obj.getString("Sk");
        System.out.println("Sk: "+sk);
        String enrollId = obj.getString("enrollment_id");
        System.out.println("enrollment_id: "+enrollId);
        String cri = obj.getString("credential_revocation_information");
        System.out.println("Cred: "+cred);
        String OUIdentifier;
        String role;
        try {
            OUIdentifier = obj.getString("organizational_unit_identifier");
        } catch (final Exception e) {
            OUIdentifier = "";
            System.out.println("!!!!!! 'organizational_unit_identifier' WAS NOT in the SignerConfig!!!!!");
        }
        try {
            role = obj.getString("role?");
        } catch (final Exception e) {
            role = "";
            System.out.println("!!!!!! 'role' WAS NOT in the SignerConfig!!!!!");
        }


        ByteString credBytes = ByteString.copyFrom(Base64.getDecoder().decode(cred));
        ByteString skBytes = ByteString.copyFrom(Base64.getDecoder().decode(sk));
        ByteString criBytes = ByteString.copyFrom(Base64.getDecoder().decode(cri));

        IdemixMSPSignerConfig.Builder builder = IdemixMSPSignerConfig.newBuilder();
        builder.setCred(credBytes);
        builder.setSk(skBytes);
        builder.setCredentialRevocationInformation(criBytes);
        builder.setEnrollmentId(enrollId);

        if (!OUIdentifier.equals(""))
            builder.setOrganizationalUnitIdentifier(OUIdentifier);
        if (!role.equals(""))
            builder.setRole(Integer.parseInt(role));


        builder.build().writeTo(new FileOutputStream(outputFilePath));





        /* IGNORE
        Credential.Builder builderCred = Credential.newBuilder();
        builderCred.mergeFrom(credBytes);
        System.out.println("\nPRINTING CRED FIELDS");
        //IdemixCredential creds = IdemixCredential.IdemixCredential();
        
        SerializedIdemixIdentity.Builder builderSerialized = SerializedIdemixIdentity.newBuilder();
        System.out.println("\nPRINTING Serialized FIELDS");
        System.out.println("NYM_X: "+builderSerialized.getNymX().toStringUtf8());
        System.out.println("NYM_Y: "+builderSerialized.getNymY().toStringUtf8());
        System.out.println("ou: "+builderSerialized.getOu().toStringUtf8());
        System.out.println("role: "+builderSerialized.getRole());
        System.out.println("proof: "+builderSerialized.getProof().toStringUtf8());

        //JsonFormat.parser().merge(signerConfigJson, builder);
        //IdemixCredential idemixCredential = new IdemixCredential() */
    }

}
