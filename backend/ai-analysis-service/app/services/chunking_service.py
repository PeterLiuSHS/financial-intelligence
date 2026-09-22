import re


def split_long_paragraph(
        paragraph: str,
        chunk_size: int
) -> list[str]:

    sentences = re.split(
        r'(?<=[.!?])\s+',
        paragraph
    )

    chunks = []
    current_sentences = []
    current_length = 0

    for sentence in sentences:
        sentence_length = len(sentence)

        if (
            current_sentences
            and current_length + sentence_length > chunk_size
        ):
            chunks.append(
                " ".join(current_sentences)
            )

            current_sentences = []
            current_length = 0

        current_sentences.append(sentence)
        current_length += sentence_length

    if current_sentences:
        chunks.append(
            " ".join(current_sentences)
        )

    return chunks

def chunk_text(
        text: str,
        chunk_size: int = 2000
) -> list[str]:

    paragraphs = [
        paragraph.strip()
        for paragraph in text.split("\n\n")
        if paragraph.strip()
    ]

    chunks = []
    current_chunk = []
    current_length = 0

    for paragraph in paragraphs:

        # If one paragraph is already too long,
        # split it by sentence boundaries first.
        if len(paragraph) > chunk_size:

            # Save the current normal chunk first.
            if current_chunk:
                chunks.append(
                    "\n\n".join(current_chunk)
                )

                current_chunk = []
                current_length = 0

            long_paragraph_chunks = split_long_paragraph(
                paragraph=paragraph,
                chunk_size=chunk_size
            )

            chunks.extend(long_paragraph_chunks)

            continue

        # Normal paragraph
        if (
            current_chunk
            and current_length + len(paragraph) > chunk_size
        ):
            chunks.append(
                "\n\n".join(current_chunk)
            )

            current_chunk = []
            current_length = 0

        current_chunk.append(paragraph)
        current_length += len(paragraph)

    if current_chunk:
        chunks.append(
            "\n\n".join(current_chunk)
        )

    return chunks
