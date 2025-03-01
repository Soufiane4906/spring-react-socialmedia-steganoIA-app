import { useFormik } from 'formik'
import React, { useContext, useState } from 'react'
import {
    Modal,
    ModalOverlay,
    ModalContent,
    ModalHeader,
    Button,
    ModalFooter,
    ModalBody,
    ModalCloseButton,
    FormLabel,
    Input,
    FormControl,
    useDisclosure,
    Textarea,
    useToast,
    Progress,
    Text
} from '@chakra-ui/react'
import PostService from '../services/PostService'
import PostImageService from '../services/PostImageService'
import AuthContext from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'

function AddPost() {
    const { user } = useContext(AuthContext)
    const [file, setFile] = useState(null)
    const [uploadProgress, setUploadProgress] = useState(0)
    const [statusMessage, setStatusMessage] = useState("")
    const { isOpen, onOpen, onClose } = useDisclosure()
    const toast = useToast()
    const token = localStorage.getItem("token")
    const postService = new PostService()
    const postImageService = new PostImageService()
    const navigate = useNavigate()

    const handleImageChange = (e) => {
        setFile(e.target.files[0])
        setStatusMessage("") // Reset status on new file
    }

    const formik = useFormik({
        initialValues: {
            userId: 0,
            description: ""
        },
        onSubmit: async (values) => {
            try {
                setStatusMessage("Creating post...")
                values.userId = user.id
                const result = await postService.add(values, token)
                const postId = result.data

                if (file) {
                    const formData = new FormData()
                    formData.append("postId", postId)
                    formData.append("image", file)

                    setStatusMessage("Checking image for AI & steganography...")

                    const response = await postImageService.upload(formData, token, (progress) => {
                        setUploadProgress(progress) // Update progress
                    })

                    if (response.error) {
                        setStatusMessage(`❌ ${response.error}`)
                        toast({
                            title: "Upload Failed",
                            description: response.error,
                            status: "error",
                            duration: 5000,
                            isClosable: true,
                        })
                        return
                    }

                    setStatusMessage("✅ Image uploaded successfully!")

                    toast({
                        title: "Post Shared",
                        status: 'success',
                        duration: 5000,
                        isClosable: true,
                    })
                    navigate(`/profile/${user.id}`)
                }
            } catch (error) {
                setStatusMessage("❌ Error sharing post!")
                toast({
                    title: "Error",
                    status: 'error',
                    duration: 5000,
                    isClosable: true,
                })
            }
        }
    })

    return (
        <>
            <Button onClick={onOpen} colorScheme={'pink'}>Share Post</Button>
            <Modal isOpen={isOpen} onClose={onClose}>
                <ModalOverlay />
                <ModalContent as={'form'} onSubmit={formik.handleSubmit}>
                    <ModalHeader>Share a post</ModalHeader>
                    <ModalCloseButton />
                    <ModalBody pb={6}>
                        <FormControl>
                            <FormLabel>Description</FormLabel>
                            <Textarea
                                placeholder='Description'
                                onChange={formik.handleChange}
                                onBlur={formik.handleBlur}
                                name='description'
                                value={formik.values.description}
                            />
                        </FormControl>

                        <FormControl mt={4}>
                            <FormLabel>Upload Image</FormLabel>
                            <Button colorScheme={'pink'} as={'label'}>
                                {file ? file.name : " Upload Image"}
                                <input hidden type={'file'} accept="image/*" onChange={handleImageChange} />
                            </Button>
                        </FormControl>

                        {uploadProgress > 0 && (
                            <Progress mt={4} value={uploadProgress} size="sm" colorScheme="pink" />
                        )}

                        {statusMessage && <Text mt={2} color="gray.600">{statusMessage}</Text>}
                    </ModalBody>

                    <ModalFooter>
                        <Button type='submit' colorScheme='pink' mr={3} disabled={!file}>
                            Share
                        </Button>
                        <Button onClick={onClose}>Cancel</Button>
                    </ModalFooter>
                </ModalContent>
            </Modal>
        </>
    )
}

export default AddPost
