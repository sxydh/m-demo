use aes_gcm::{
    Aes256Gcm, Nonce,
    aead::{Aead, KeyInit},
};
use anyhow::{Result, anyhow};
use base64::{Engine as _, engine::general_purpose};
use pbkdf2::pbkdf2_hmac;
use rand::{RngCore, rngs::OsRng};
use rayon::prelude::*;
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
        let encrypt_password = encrypt_password.trim();

        print!("Decryption password: ");
        io::stdout().flush()?;
        let mut decrypt_password = String::new();
        io::stdin().read_line(&mut decrypt_password)?;
        let decrypt_password = decrypt_password.trim();

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
            println!("1 - Decrypt file");
            println!("2 - Update file");
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
                    let file_path = file_path.trim().trim_matches('"');

                    print!("Password: ");
                    io::stdout().flush()?;
                    let mut password = String::new();
                    io::stdin().read_line(&mut password)?;
                    let password = password.trim();

                    match std::fs::read_to_string(file_path) {
                        Ok(file_content) => {
                            let lines: Vec<&str> = file_content.lines().collect();
                            let new_lines: Vec<String> = lines
                                .par_iter()
                                .map(|line| {
                                    let line = *line;
                                    let first_non_space = line.find(|c: char| !c.is_whitespace()).unwrap_or(0);
                                    let (space_part, content_part) = line.split_at(first_non_space);
                                    match decrypt_string(&password, content_part.trim()) {
                                        Ok(dec) => format!("{}{}", space_part, dec),
                                        Err(_) => line.to_string(),
                                    }
                                })
                                .collect();

                            println!("\n{}\n", new_lines.join("\n"));
                        }
                        Err(e) => eprintln!("\n{}\n", e),
                    }
                }
                "2" => {
                    println!("File path:");
                    io::stdout().flush()?;
                    let mut file_path = String::new();
                    io::stdin().read_line(&mut file_path)?;
                    let file_path = file_path.trim().trim_matches('"');

                    print!("Old password: ");
                    io::stdout().flush()?;
                    let mut old_password = String::new();
                    io::stdin().read_line(&mut old_password)?;
                    let old_password = old_password.trim();

                    print!("New password: ");
                    io::stdout().flush()?;
                    let mut new_password = String::new();
                    io::stdin().read_line(&mut new_password)?;
                    let new_password = new_password.trim();

                    match std::fs::read_to_string(file_path) {
                        Ok(file_content) => {
                            let lines: Vec<&str> = file_content.lines().collect();
                            let new_lines: Vec<String> = lines
                                .par_iter()
                                .map(|line| {
                                    let line = *line;
                                    let first_non_space = line.find(|c: char| !c.is_whitespace()).unwrap_or(0);
                                    let (space_part, content_part) = line.split_at(first_non_space);
                                    match decrypt_string(&old_password, content_part.trim()) {
                                        Ok(dec) => match encrypt_string(&new_password, &dec) {
                                            Ok(enc) => format!("{}{}", space_part, enc),
                                            Err(_) => line.to_string()
                                        },
                                        Err(_) => line.to_string()
                                    }
                                })
                                .collect();

                            match std::fs::write(format!("{}.updated", file_path), new_lines.join("\n")) {
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
