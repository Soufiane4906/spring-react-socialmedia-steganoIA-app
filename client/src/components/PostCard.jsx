import {
    Flex,
    Card,
    CardHeader,
    CardBody,
    CardFooter,
    Button,
    Avatar,
    Box,
    Heading,
    Text,
    Image,
    useDisclosure,
    AlertDialog,
    AlertDialogOverlay,
    AlertDialogContent,
    AlertDialogHeader,
    AlertDialogBody,
    AlertDialogFooter,
  } from "@chakra-ui/react";
  import { useCallback, useContext, useEffect, useState, useRef } from "react";
  import { BiLike, BiChat, BiShare, BiTrash } from "react-icons/bi";
  import { Link } from "react-router-dom";
  import AuthContext from "../context/AuthContext";
  import LikeService from "../services/LikeService";
  import PostService from "../services/PostService";
  import CommentModal from "./CommentModal";

  function PostCard({
    userName,
    userImage,
    description,
    postImage,
    postId,
    userId,
  }) {
    const likeService = new LikeService();
    const postService = new PostService();
    const { user } = useContext(AuthContext);
    const [isLiked, setIsLiked] = useState(false);
    const [likes, setLikes] = useState([]);

    debugger
    // Gestion de la boîte de dialogue de suppression
    const { isOpen, onOpen, onClose } = useDisclosure();
    const cancelRef = useRef();

    const handleLike = async () => {
      try {
        await likeService.add(user.id, postId, localStorage.getItem("token"));
        getLikes();
      } catch (error) {
        console.log(error);
      }
    };

    const handleUnlike = async () => {
      try {
        await likeService.delete(user.id, postId, localStorage.getItem("token"));
        getLikes();
      } catch (error) {
        console.log(error);
      }
    };

    const handleDelete = async () => {
      try {
        await postService.delete(postId, localStorage.getItem("token"));
        getLikes();
        onClose(); // Fermer la boîte de dialogue après suppression
      } catch (error) {
        console.log(error);
      }
    };

    const checkIsLiked = useCallback(async () => {
      try {
        const result = await likeService.isLiked(
          user.id,
          postId,
          localStorage.getItem("token")
        );
        setIsLiked(result.data);
      } catch (error) {
        console.log(error);
      }
    }, [user.id, postId, likes.length]);

    const getLikes = useCallback(async () => {
      try {
        const result = await likeService.getLikesByPost(
          postId,
          localStorage.getItem("token")
        );
        setLikes(result.data);
      } catch (error) {
        console.log(error);
      }
    }, [postId]);

    useEffect(() => {
      checkIsLiked();
      getLikes();
    }, [checkIsLiked, getLikes]);

    return (
      <Card maxW="lg">
        <CardHeader as={Link} to={`/profile/${userId}`}>
          <Flex spacing="4">
            <Flex flex="1" gap="4" alignItems="center" flexWrap="wrap">
              <Avatar name={userName} src={userImage} />

              <Box>
                <Heading size="sm">{userName}</Heading>
              </Box>
            </Flex>
          </Flex>
        </CardHeader>
        <CardBody>
          <Text>{description}</Text>
        </CardBody>
        {postImage && (
          <Image
            maxW={"md"}
            maxH={"sm"}
            objectFit="contain"
            src={postImage}
            fallback={null}
          />
        )}

        <CardFooter
          justify="space-between"
          flexWrap="wrap"
          sx={{
            "& > button": {
              minW: "136px",
            },
          }}
        >
          {isLiked ? (
            <Button
              onClick={() => handleUnlike()}
              flex="1"
              colorScheme={"pink"}
              leftIcon={<BiLike />}
            >
              Like {likes.length}
            </Button>
          ) : (
            <Button
              onClick={() => handleLike()}
              flex="1"
              variant="ghost"
              leftIcon={<BiLike />}
            >
              Like {likes.length}
            </Button>
          )}

          <CommentModal postId={postId} />

          {user.id === userId && (
            <>
              <Button
                onClick={onOpen}
                flex="1"
                colorScheme={"red"}
                leftIcon={<BiTrash />}
              >
                Delete
              </Button>

              {/* Boîte de dialogue de confirmation */}
              <AlertDialog
                isOpen={isOpen}
                leastDestructiveRef={cancelRef}
                onClose={onClose}
              >
                <AlertDialogOverlay>
                  <AlertDialogContent>
                    <AlertDialogHeader fontSize="lg" fontWeight="bold">
                      Supprimer le post
                    </AlertDialogHeader>

                    <AlertDialogBody>
                      Es-tu sûr de vouloir supprimer ce post ? Cette action est
                      irréversible.
                    </AlertDialogBody>

                    <AlertDialogFooter>
                      <Button ref={cancelRef} onClick={onClose}>
                        Annuler
                      </Button>
                      <Button colorScheme="red" onClick={handleDelete} ml={3}>
                        Supprimer
                      </Button>
                    </AlertDialogFooter>
                  </AlertDialogContent>
                </AlertDialogOverlay>
              </AlertDialog>
            </>
          )}

          <Button flex="1" variant="ghost" leftIcon={<BiChat />}>
            Comment
          </Button>
          <Button flex="1" variant="ghost" leftIcon={<BiShare />}>
            Share
          </Button>
        </CardFooter>
      </Card>
    );
  }

  export default PostCard;
