use aes_gcm::{
    Aes256Gcm, Nonce,
    aead::{Aead, KeyInit},
};
use anyhow::{Result, anyhow};
use base64::{Engine as _, engine::general_purpose};
use pbkdf2::pbkdf2_hmac;
use rand::{RngCore, rngs::OsRng};
use sha2::Sha256;
use std::io::{self, Write};

const NONCE_SIZE: usize = 12;
const SALT_SIZE: usize = 16;
const KEY_SIZE: usize = 32;
const PBKDF2_ROUNDS: u32 = 100_000;

fn get_key(password: &str, salt: &[u8]) -> [u8; KEY_SIZE] {
    let mut key = [0u8; KEY_SIZE];
    pbkdf2_hmac::<Sha256>(password.as_bytes(), salt, PBKDF2_ROUNDS, &mut key);
    key
}

fn encrypt_string(password: &str, plaintext: &str) -> Result<String> {
    let mut salt = [0u8; SALT_SIZE];
    let mut nonce = [0u8; NONCE_SIZE];
    OsRng.fill_bytes(&mut salt);
    OsRng.fill_bytes(&mut nonce);

    let key = get_key(password, &salt);
    let cipher = Aes256Gcm::new_from_slice(&key).map_err(anyhow::Error::msg)?;

    let encrypted_bytes = cipher
        .encrypt(Nonce::from_slice(&nonce), plaintext.as_bytes())
        .map_err(anyhow::Error::msg)?;

    let mut combined_bytes = Vec::new();
    combined_bytes.extend_from_slice(&salt);
    combined_bytes.extend_from_slice(&nonce);
    combined_bytes.extend_from_slice(&encrypted_bytes);

    Ok(general_purpose::STANDARD.encode(&combined_bytes))
}

fn decrypt_string(password: &str, encrypted_b64: &str) -> Result<String> {
    let combined_bytes = general_purpose::STANDARD.decode(encrypted_b64)?;

    if combined_bytes.len() < SALT_SIZE + NONCE_SIZE {
        return Err(anyhow!("Invalid encrypted data format"));
    }

    let (salt, rest) = combined_bytes.split_at(SALT_SIZE);
    let (nonce, encrypted_bytes) = rest.split_at(NONCE_SIZE);

    let key = get_key(password, salt);
    let cipher = Aes256Gcm::new_from_slice(&key).map_err(anyhow::Error::msg)?;
    let decrypted_bytes = cipher
        .decrypt(Nonce::from_slice(nonce), encrypted_bytes)
        .map_err(anyhow::Error::msg)?;

    Ok(String::from_utf8(decrypted_bytes)?)
}

fn main() -> Result<()> {
    loop {
        println!("-----------------------");
        print!("Encryption password: ");
        io::stdout().flush()?;
        let mut encrypt_password = String::new();
        io::stdin().read_line(&mut encrypt_password)?;
        let encrypt_password = encrypt_password.trim().to_string();

        print!("Decryption password: ");
        io::stdout().flush()?;
        let mut decrypt_password = String::new();
        io::stdin().read_line(&mut decrypt_password)?;
        let decrypt_password = decrypt_password.trim().to_string();

        if !encrypt_password.is_empty() || !decrypt_password.is_empty() {
            println!("Input:");
            io::stdout().flush()?;
            let mut input = String::new();
            io::stdin().read_line(&mut input)?;
            let input = input.trim();

            if !encrypt_password.is_empty() {
                match encrypt_string(&encrypt_password, input) {
                    Ok(enc) => println!("\n{}\n", enc),
                    Err(e) => eprintln!("\n{}\n", e),
                }
            } else if !decrypt_password.is_empty() {
                match decrypt_string(&decrypt_password, input) {
                    Ok(dec) => println!("\n{}\n", dec),
                    Err(e) => eprintln!("\n{}\n", e),
                }
            }
        } else {
            println!("\n-----------------------");
            println!("1 - Decrypt TXT file");
            io::stdout().flush()?;
            let mut function_code = String::new();
            io::stdin().read_line(&mut function_code)?;
            let function_code = function_code.trim();
            match function_code {
                "1" => {
                    println!("File path:");
                    io::stdout().flush()?;
                    let mut file_path = String::new();
                    io::stdin().read_line(&mut file_path)?;
                    print!("Password: ");
                    io::stdout().flush()?;
                    let mut password = String::new();
                    io::stdin().read_line(&mut password)?;

                    let file_path = file_path.trim();
                    match std::fs::read_to_string(file_path) {
                        Ok(file_content) => {
                            let lines: Vec<&str> = file_content.lines().collect();
                            let mut new_lines: Vec<String> = Vec::new();
                            for line in lines {
                                let first_non_space =
                                    line.find(|c: char| !c.is_whitespace()).unwrap_or(0);
                                let (space_part, content_part) = line.split_at(first_non_space);
                                match decrypt_string(&password, content_part) {
                                    Ok(dec) => new_lines.push(format!("{}{}", space_part, dec)),
                                    Err(_) => new_lines.push(line.to_string()),
                                }
                            }

                            let new_file_content = new_lines.join("\n");
                            match std::fs::write(format!("{}.dec", file_path), new_file_content) {
                                Ok(_) => (),
                                Err(e) => eprintln!("\n{}\n", e),
                            }
                        }
                        Err(e) => eprintln!("\n{}\n", e),
                    }
                }
                _ => (),
            }
        }
    }
}
